package dev.alexisreyna.artisanalbrew.web.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.alexisreyna.artisanalbrew.application.config.BlockchainNetworkOptions;
import dev.alexisreyna.artisanalbrew.application.config.ChainDefinition;
import dev.alexisreyna.artisanalbrew.application.config.ChainFamily;
import dev.alexisreyna.artisanalbrew.application.config.ChainRegistry;
import dev.alexisreyna.artisanalbrew.application.repository.UnitOfWork;
import dev.alexisreyna.artisanalbrew.application.repository.WalletStatusEventRepository;
import dev.alexisreyna.artisanalbrew.application.service.IdentityAccountService;
import dev.alexisreyna.artisanalbrew.application.service.SqsMessagePublisher;
import dev.alexisreyna.artisanalbrew.application.service.WalletAccountResult;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.WalletAddressRules;
import dev.alexisreyna.artisanalbrew.application.service.wallet.EvmChallengeVerificationError;
import dev.alexisreyna.artisanalbrew.application.service.wallet.EvmWalletChallenge;
import dev.alexisreyna.artisanalbrew.application.service.wallet.EvmWalletChallengeService;
import dev.alexisreyna.artisanalbrew.application.service.wallet.SolanaChallengeVerificationError;
import dev.alexisreyna.artisanalbrew.application.service.wallet.SolanaWalletChallenge;
import dev.alexisreyna.artisanalbrew.application.service.wallet.SolanaWalletChallengeService;
import dev.alexisreyna.artisanalbrew.domain.entities.WalletStatusEvent;
import dev.alexisreyna.artisanalbrew.web.auth.WalletSession;
import dev.alexisreyna.artisanalbrew.web.chain.SelectedChainAccessor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Port of {@code WalletAuthController} ({@code Web/Controllers}): EVM and
 * Solana challenge flows, wallet sessions, and login status publishing.
 */
@RestController
@RequestMapping("/api/wallet-auth")
public class WalletAuthController {
    private static final Logger log = LoggerFactory.getLogger(WalletAuthController.class);

    private final EvmWalletChallengeService evmChallenges;
    private final SolanaWalletChallengeService solanaChallenges;
    private final ChainRegistry chains;
    private final SelectedChainAccessor selectedChain;
    private final BlockchainNetworkOptions legacyNetwork;
    private final UnitOfWork unitOfWork;
    private final SqsMessagePublisher publisher;
    private final WalletStatusEventRepository statusRepository;
    private final IdentityAccountService identityAccounts;
    private final ObjectMapper mapper;

    public WalletAuthController(EvmWalletChallengeService evmChallenges,
                                SolanaWalletChallengeService solanaChallenges,
                                ChainRegistry chains,
                                SelectedChainAccessor selectedChain,
                                BlockchainNetworkOptions legacyNetwork,
                                UnitOfWork unitOfWork,
                                SqsMessagePublisher publisher,
                                WalletStatusEventRepository statusRepository,
                                IdentityAccountService identityAccounts,
                                ObjectMapper mapper) {
        this.evmChallenges = evmChallenges;
        this.solanaChallenges = solanaChallenges;
        this.chains = chains;
        this.selectedChain = selectedChain;
        this.legacyNetwork = legacyNetwork;
        this.unitOfWork = unitOfWork;
        this.publisher = publisher;
        this.statusRepository = statusRepository;
        this.identityAccounts = identityAccounts;
        this.mapper = mapper;
    }

    @PostMapping("/solana/challenge")
    public ResponseEntity<?> solanaChallenge(@RequestBody SolanaChallengeRequest request, HttpServletRequest http) {
        ChainDefinition chain = chains.tryGet(request.chainKey()).filter(c -> c.isEnabled()
                        && c.getFamily() == ChainFamily.SOLANA).orElse(null);
        if (chain == null || !chain.getCapabilities().walletLogin()) {
            return ResponseEntity.status(409)
                    .body(Map.of("error", "Solana wallet login is not enabled for this chain."));
        }
        try {
            SolanaWalletChallenge challenge =
                    solanaChallenges.create(request.address(), chain, originOf(http));
            return ResponseEntity.ok(new SolanaChallengeResponse(challenge.message(), challenge.nonce(),
                    chain.getKey(), chain.getSolanaCluster(), challenge.issuedAt(), challenge.expiresAt()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "A valid Solana public key is required."));
        }
    }

    @PostMapping("/solana/verify")
    public ResponseEntity<?> solanaVerify(@RequestBody SolanaVerifyRequest request, HttpServletRequest http,
                                          HttpSession session) {
        ChainDefinition chain = chains.tryGet(request.chainKey()).filter(c -> c.isEnabled()
                        && c.getFamily() == ChainFamily.SOLANA).orElse(null);
        if (chain == null || !chain.getCapabilities().walletLogin()) {
            return ResponseEntity.status(409)
                    .body(Map.of("error", "Solana wallet login is not enabled for this chain."));
        }
        String origin = originOf(http);
        try (var tx = unitOfWork.beginTransaction()) {
            SolanaChallengeVerificationError error = solanaChallenges.verify(
                    request.address(), request.message(), request.nonce(), chain.getKey(), origin, request.signature());
            if (error != SolanaChallengeVerificationError.NONE) {
                return error == SolanaChallengeVerificationError.INVALID_SIGNATURE
                        ? ResponseEntity.status(401).body(Map.of("error", "The Solana signature is invalid."))
                        : ResponseEntity.badRequest()
                                .body(Map.of("error", "The Solana login challenge is expired or does not match."));
            }
            WalletAccountResult result = identityAccounts.findOrCreateAndLinkWallet(
                    request.address(), null, request.walletName(), chain.familyName());
            if (!result.succeeded()) {
                return result.walletAlreadyLinkedToAnotherAccount()
                        ? ResponseEntity.status(409).body(
                                Map.of("error", "This Solana wallet identity is already verified for another user."))
                        : ResponseEntity.internalServerError()
                                .body(Map.of("error", result.error() == null
                                        ? "Could not update the Solana wallet identity." : result.error()));
            }
            tx.commit();
            identityAccounts.signIn(result.account().id(), true);
        }
        session.setAttribute(WalletSession.WALLET_ADDRESS, request.address());
        session.setAttribute(WalletSession.WALLET_CHAIN_KEY, chain.getKey());
        return ResponseEntity.ok(new WalletVerifyResponse(true, request.address(), "/profile?tour=true", false, false, null));
    }

    @GetMapping("/status")
    public ResponseEntity<?> status(@RequestParam String walletAddress) {
        String address = WalletAddressRules.tryNormalizeWallet(walletAddress).orElse(null);
        if (address == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "A valid wallet address is required."));
        }
        return statusRepository.getLatestForWallet(address)
                .map(event -> ResponseEntity.ok((Object) describe(event)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/challenge")
    public ResponseEntity<?> challenge(@RequestBody WalletChallengeRequest request, HttpServletRequest http) {
        ChainDefinition chain = resolveSelectedEvmChain(request.chainKey());
        if (chain == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Select an enabled EVM network before connecting an EVM wallet."));
        }
        String address = WalletAddressRules.tryNormalizeWallet(request.address()).orElse(null);
        if (address == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "A valid wallet address is required."));
        }
        EvmWalletChallenge challenge = evmChallenges.createChallenge(address, chain.getEvmChainId(),
                chain.getDisplayName(), originOf(http), chain.getKey(), chain.familyName());
        publishStatus("wallet-login.challenge-created", "ChallengeCreated", address, request.walletName(), null, http);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", challenge.message());
        body.put("nonce", challenge.nonce());
        body.put("chainId", chain.getEvmChainId());
        body.put("chainIdHex", chain.getEvmChainIdHex());
        body.put("networkName", chain.getDisplayName());
        body.put("rpcUrl", chain.getPublicRpcUrl());
        body.put("explorerUrl", explorerBaseUrl(chain));
        body.put("currencyName", chain.getNativeCurrencyName());
        body.put("currencySymbol", chain.getNativeCurrencySymbol());
        body.put("currencyDecimals", chain.getNativeCurrencyDecimals());
        body.put("chainKey", chain.getKey());
        body.put("family", chain.familyName());
        return ResponseEntity.ok(body);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody WalletVerifyRequest request, HttpServletRequest http,
                                    HttpSession session) {
        ChainDefinition chain = resolveSelectedEvmChain(request.chainKey());
        if (chain == null || request.chainId() != chain.getEvmChainId()) {
            String name = chain == null ? "the selected EVM network" : chain.getDisplayName();
            publishStatus("wallet-login.failed", "Failed", request.address(), request.walletName(),
                    "Wallet must be connected to " + name + ".", http);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Wallet must be connected to " + name + "."));
        }
        String address = WalletAddressRules.tryNormalizeWallet(request.address()).orElse(null);
        if (address == null) {
            publishStatus("wallet-login.failed", "Failed", request.address(), request.walletName(),
                    "A valid wallet address is required.", http);
            return ResponseEntity.badRequest().body(Map.of("error", "A valid wallet address is required."));
        }
        var verification = evmChallenges.verify(address, request.message(), request.nonce(),
                request.chainId(), request.signature(), chain.getKey(), chain.familyName());
        if (verification.error() == EvmChallengeVerificationError.EXPIRED) {
            publishStatus("wallet-login.failed", "Failed", address, request.walletName(),
                    "The wallet login challenge expired.", http);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "The wallet login challenge expired. Please try again."));
        }
        if (verification.error() == EvmChallengeVerificationError.MISMATCH) {
            publishStatus("wallet-login.failed", "Failed", address, request.walletName(),
                    "The signed wallet login challenge does not match this session.", http);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "The signed wallet login challenge does not match this session."));
        }
        if (verification.error() == EvmChallengeVerificationError.INVALID_SIGNATURE) {
            publishStatus("wallet-login.failed", "Failed", address, request.walletName(),
                    "The signature was not produced by the requested wallet.", http);
            return ResponseEntity.status(401)
                    .body(Map.of("error", "The signature was not produced by the requested wallet."));
        }
        String checksum = WalletAddressRules.toChecksumAddress(address);
        WalletAccountResult result = identityAccounts.findOrCreateAndLinkWallet(
                checksum, chain.getEvmChainId(), request.walletName(), chain.familyName());
        if (!result.succeeded()) {
            publishStatus("wallet-login.failed", "Failed", checksum, request.walletName(),
                    result.error() == null ? "Could not update the wallet user." : result.error(), http);
            return result.walletAlreadyLinkedToAnotherAccount()
                    ? ResponseEntity.status(409).body(
                            Map.of("error", "This wallet identity is already verified for another user."))
                    : ResponseEntity.internalServerError().body(Map.of("error",
                            result.error() == null ? "Could not update the wallet user." : result.error()));
        }
        identityAccounts.signIn(result.account().id(), true);
        session.setAttribute(WalletSession.WALLET_ADDRESS, checksum);
        session.setAttribute(WalletSession.WALLET_CHAIN_KEY, chain.getKey());
        StatusResult status = recordStatus("wallet-login.verified", "Verified", checksum,
                request.walletName(), null, http);
        return ResponseEntity.ok(new WalletVerifyResponse(true, checksum, "/profile?tour=true",
                status.stored(), status.published(), status.awsMessageId()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session, HttpServletRequest http) {
        String wallet = WalletSession.currentWallet(session);
        identityAccounts.signOut();
        session.removeAttribute(WalletSession.WALLET_ADDRESS);
        if (!wallet.isBlank()) {
            publishStatus("wallet-login.logout", "LoggedOut", wallet, null, null, http);
        }
        return ResponseEntity.status(302).location(URI.create("/")).build();
    }

    private ChainDefinition resolveSelectedEvmChain(String requestedChainKey) {
        String key = requestedChainKey == null || requestedChainKey.isBlank()
                ? selectedChain.selectedChainKey() : requestedChainKey.trim();
        return chains.tryGet(key)
                .filter(c -> c.isEnabled() && c.getFamily() == ChainFamily.EVM && c.getCapabilities().walletLogin())
                .orElse(null);
    }

    private static String originOf(HttpServletRequest request) {
        String host = request.getHeader("Host");
        if (host == null || host.isBlank()) {
            host = request.getServerName();
        }
        return request.getScheme() + "://" + host;
    }

    private static String explorerBaseUrl(ChainDefinition chain) {
        String template = chain.getExplorerAddressTemplate();
        int marker = template.toLowerCase(java.util.Locale.ROOT).indexOf("/address/");
        return marker > 0 ? template.substring(0, marker) : template;
    }

    private void publishStatus(String eventName, String status, String walletAddress,
                               String walletName, String reason, HttpServletRequest http) {
        recordStatus(eventName, status, walletAddress, walletName, reason, http);
    }

    private StatusResult recordStatus(String eventName, String status, String walletAddress,
                                      String walletName, String reason, HttpServletRequest http) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("eventName", eventName);
            payload.put("chainId", legacyNetwork.getChainId());
            payload.put("networkName", legacyNetwork.getNetworkName());
            payload.put("walletName", walletName);
            payload.put("reason", reason);
            payload.put("userAgent", http.getHeader("User-Agent"));
            payload.put("remoteIpAddress", http.getRemoteAddr());
            String payloadJson = mapper.writeValueAsString(payload);

            boolean stored = false;
            WalletStatusEvent event = null;
            String normalized = WalletAddressRules.tryNormalizeWallet(walletAddress).orElse(null);
            if (normalized != null) {
                event = new WalletStatusEvent();
                event.setWalletAddress(normalized);
                event.setStatus(status);
                event.setEventType(eventName);
                event.setPayloadJson(payloadJson);
                event.setCreatedAt(Instant.now());
                statusRepository.add(event);
                stored = true;
            }
            String awsMessageId = null;
            try {
                awsMessageId = publisher.publish(Map.of(
                                "walletAddress", walletAddress, "status", status,
                                "eventType", eventName, "createdAt", Instant.now().toString()))
                        .orElse(null);
                if (awsMessageId != null && !awsMessageId.isBlank() && event != null) {
                    statusRepository.markPublishedToAws(event.getId(), awsMessageId, Instant.now());
                }
            } catch (Exception e) {
                log.error("Wallet auth status was stored but could not be published. Status={}", status, e);
            }
            return new StatusResult(stored, awsMessageId != null && !awsMessageId.isBlank(), awsMessageId);
        } catch (Exception e) {
            log.error("Could not publish wallet auth status {} for {}", status, walletAddress, e);
            return new StatusResult(false, false, null);
        }
    }

    private Map<String, Object> describe(WalletStatusEvent event) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", event.getId());
        body.put("walletAddress", event.getWalletAddress());
        body.put("status", event.getStatus());
        body.put("eventType", event.getEventType());
        try {
            body.put("payload", event.getPayloadJson() == null ? null : mapper.readTree(event.getPayloadJson()));
        } catch (Exception e) {
            body.put("payload", null);
        }
        body.put("createdAt", event.getCreatedAt().toString());
        body.put("publishedToAwsAtUtc",
                event.getPublishedToAwsAtUtc() == null ? null : event.getPublishedToAwsAtUtc().toString());
        body.put("awsMessageId", event.getAwsMessageId());
        return body;
    }

    public record SolanaChallengeRequest(String address, String chainKey, String walletName) {
    }

    public record SolanaChallengeResponse(String message, String nonce, String chainKey, String cluster,
                                          OffsetDateTime issuedAt, OffsetDateTime expiresAt) {
    }

    public record SolanaVerifyRequest(String address, String signature, String message, String nonce,
                                      String chainKey, String walletName) {
    }

    public record WalletChallengeRequest(String address, String walletName, String chainKey) {
    }

    public record WalletVerifyRequest(String address, String signature, String message, String nonce,
                                      int chainId, String walletName, String chainKey) {
    }

    public record WalletVerifyResponse(boolean success, String address, String redirectUrl,
                                       boolean statusStored, boolean statusPublished, String awsMessageId) {
    }

    private record StatusResult(boolean stored, boolean published, String awsMessageId) {
    }
}
