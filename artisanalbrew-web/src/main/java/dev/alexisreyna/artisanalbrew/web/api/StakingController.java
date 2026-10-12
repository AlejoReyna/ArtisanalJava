package dev.alexisreyna.artisanalbrew.web.api;

import dev.alexisreyna.artisanalbrew.application.config.BlockchainNetworkOptions;
import dev.alexisreyna.artisanalbrew.application.service.IdentityAccount;
import dev.alexisreyna.artisanalbrew.application.service.IdentityAccountService;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.CoffeeWeb3Service;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.StakingAmountRules;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.StakingLedgerService;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.StakingTransactionType;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.StakingVerificationResult;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.TransactionVerificationStatus;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.WalletAddressRules;
import dev.alexisreyna.artisanalbrew.application.service.wallet.EvmChallengeVerificationError;
import dev.alexisreyna.artisanalbrew.application.service.wallet.EvmWalletChallengeService;
import dev.alexisreyna.artisanalbrew.web.auth.WalletSession;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;

/** Port of {@code StakingController} ({@code Web/Controllers}, {@code staking/*}). */
@RestController
@RequestMapping("/staking")
public class StakingController {
    private final CoffeeWeb3Service web3;
    private final StakingLedgerService ledger;
    private final BlockchainNetworkOptions chain;
    private final IdentityAccountService identityAccounts;
    private final EvmWalletChallengeService challenges;

    public StakingController(CoffeeWeb3Service web3, StakingLedgerService ledger, BlockchainNetworkOptions chain,
                             IdentityAccountService identityAccounts, EvmWalletChallengeService challenges) {
        this.web3 = web3;
        this.ledger = ledger;
        this.chain = chain;
        this.identityAccounts = identityAccounts;
        this.challenges = challenges;
    }

    @GetMapping("/api/coffee-balance")
    public ResponseEntity<?> coffeeBalance(@RequestParam String walletAddress) {
        String checksum = WalletAddressRules.tryNormalizeWallet(walletAddress).orElse(null);
        if (checksum == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "A valid wallet address is required."));
        }
        return ResponseEntity.ok(Map.of("walletAddress", checksum,
                "balance", web3.getCoffeeCoinBalance(checksum), "symbol", "COFFEE"));
    }

    @GetMapping("/api/dashboard")
    public ResponseEntity<?> dashboard(@RequestParam(required = false) String walletAddress, HttpSession session) {
        String wallet = walletAddress != null && !walletAddress.isBlank()
                ? walletAddress : WalletSession.currentWallet(session);
        if (wallet == null || wallet.isBlank()) {
            return ResponseEntity.ok(new dev.alexisreyna.artisanalbrew.application.service.blockchain.CoffeeDashboardModel());
        }
        String checksum = WalletAddressRules.tryNormalizeWallet(wallet).orElse(null);
        if (checksum == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "A valid wallet address is required."));
        }
        return ResponseEntity.ok(web3.getDashboardData(checksum));
    }

    @GetMapping("/api/position")
    public ResponseEntity<?> position(@RequestParam(required = false) String walletAddress, HttpSession session) {
        return dashboard(walletAddress, session);
    }

    @PostMapping("/api/record-stake")
    public ResponseEntity<?> recordStake(@RequestBody StakingTransactionRequest request, HttpSession session,
                                         Authentication authentication) {
        return record(request, StakingTransactionType.STAKE, session, authentication);
    }

    @PostMapping("/api/record-unstake")
    public ResponseEntity<?> recordUnstake(@RequestBody StakingTransactionRequest request, HttpSession session,
                                           Authentication authentication) {
        return record(request, StakingTransactionType.UNSTAKE, session, authentication);
    }

    @PostMapping("/api/record-claim")
    public ResponseEntity<?> recordClaim(@RequestBody StakingClaimRequest request, HttpSession session,
                                         Authentication authentication) {
        if (!isStakingConfigured()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Configure a payment token and staking pool contract before staking."));
        }
        String wallet = WalletAddressRules.tryNormalizeWallet(request.walletAddress()).orElse(null);
        if (wallet == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "A valid wallet address is required."));
        }
        String sessionWallet = resolveSessionWallet(session, authentication);
        if (sessionWallet == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Connect or sign in with your wallet before recording staking activity."));
        }
        if (!sessionWallet.equalsIgnoreCase(wallet)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "The connected wallet does not match the staking session wallet."));
        }
        String transactionHash = WalletAddressRules.tryNormalizeTransactionHash(request.transactionHash()).orElse(null);
        if (transactionHash == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "A valid staking transaction hash is required."));
        }
        var result = ledger.record(chain, wallet, transactionHash, StakingTransactionType.CLAIM, null);
        return toActionResult(result, "Claim transaction could not be verified on-chain.");
    }

    @PostMapping("/save-wallet-session")
    public ResponseEntity<?> saveWalletSession(@RequestBody SaveWalletSessionRequest request, HttpSession session,
                                               Authentication authentication) {
        if (request.chainId() != chain.getChainId()) {
            return ResponseEntity.badRequest().body(Map.of("error",
                    "Connect your wallet to " + chain.getNetworkName() + " before starting an allocation."));
        }
        String wallet = WalletAddressRules.tryNormalizeWallet(request.walletAddress()).orElse(null);
        if (wallet == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "A valid wallet address is required."));
        }
        if (!isWalletAuthenticated(wallet, authentication)) {
            if (request.signature() == null || request.signature().isBlank()
                    || request.message() == null || request.message().isBlank()
                    || request.nonce() == null || request.nonce().isBlank()) {
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Sign the wallet ownership challenge before connecting for staking."));
            }
            var verification = challenges.verify(wallet, request.message(), request.nonce(),
                    request.chainId(), request.signature());
            if (verification.error() != EvmChallengeVerificationError.NONE) {
                return ResponseEntity.status(401).body(Map.of("error", "Wallet ownership could not be verified."));
            }
        }
        session.setAttribute(WalletSession.WALLET_ADDRESS, wallet);
        return ResponseEntity.ok(Map.of());
    }

    @PostMapping("/clear-wallet-session")
    public ResponseEntity<?> clearWalletSession(HttpSession session) {
        session.removeAttribute(WalletSession.WALLET_ADDRESS);
        return ResponseEntity.status(302).location(URI.create("/staking")).build();
    }

    private ResponseEntity<?> record(StakingTransactionRequest request, StakingTransactionType type,
                                     HttpSession session, Authentication authentication) {
        if (!isStakingConfigured()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Configure a payment token and staking pool contract before staking."));
        }
        String wallet = WalletAddressRules.tryNormalizeWallet(request.walletAddress()).orElse(null);
        if (wallet == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "A valid wallet address is required."));
        }
        String sessionWallet = resolveSessionWallet(session, authentication);
        if (sessionWallet == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Connect or sign in with your wallet before recording staking activity."));
        }
        if (!sessionWallet.equalsIgnoreCase(wallet)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "The connected wallet does not match the staking session wallet."));
        }
        if (!isValidAmountForType(type, request.amount())) {
            return ResponseEntity.badRequest().body(Map.of("error",
                    type == StakingTransactionType.STAKE
                            ? "Staking amount must be greater than zero."
                            : "Unstake amount must be greater than zero."));
        }
        String transactionHash = WalletAddressRules.tryNormalizeTransactionHash(request.transactionHash()).orElse(null);
        if (transactionHash == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "A valid staking transaction hash is required."));
        }
        var result = ledger.record(chain, wallet, transactionHash, type, request.amount());
        return toActionResult(result, "Staking transaction could not be verified on-chain.");
    }

    private ResponseEntity<?> toActionResult(StakingLedgerService.RecordResult result, String failureMessage) {
        return switch (result.status()) {
            case RECORDED -> {
                var entry = result.entry();
                yield ResponseEntity.ok(Map.of("success", true,
                        "transactionHash", entry.getTransactionHash(),
                        "actionType", entry.getActionType(),
                        "amount", entry.getAmount(),
                        "explorerUrl", entry.getExplorerUrl() == null ? "" : entry.getExplorerUrl()));
            }
            case ALREADY_RECORDED -> ResponseEntity.status(409)
                    .body(Map.of("error", "This staking transaction has already been recorded."));
            case PENDING_CONFIRMATIONS -> {
                StakingVerificationResult verification = result.verification();
                yield ResponseEntity.status(202).body(Map.of("success", false,
                        "status", "pending_confirmations",
                        "confirmations", verification == null ? 0 : verification.confirmations(),
                        "requiredConfirmations", verification == null ? 0 : verification.requiredConfirmations()));
            }
            default -> ResponseEntity.badRequest().body(Map.of("error", failureMessage));
        };
    }

    private static boolean isValidAmountForType(StakingTransactionType type, java.math.BigDecimal amount) {
        return type == StakingTransactionType.STAKE ? StakingAmountRules.isValidStakeAmount(amount)
                : amount != null && amount.signum() > 0;
    }

    private boolean isStakingConfigured() {
        return WalletAddressRules.isConfiguredAddress(chain.effectivePaymentTokenContract())
                && WalletAddressRules.isConfiguredAddress(chain.getStakingPoolContract());
    }

    private boolean isWalletAuthenticated(String wallet, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        String claimWallet = null;
        if (authentication instanceof dev.alexisreyna.artisanalbrew.infrastructure.security.WalletAuthentication w) {
            claimWallet = w.walletAddress();
        }
        if (claimWallet == null || claimWallet.isBlank()) {
            claimWallet = authentication.getName();
        }
        String normalizedClaim = WalletAddressRules.tryNormalizeWallet(claimWallet).orElse(null);
        if (normalizedClaim != null && normalizedClaim.equalsIgnoreCase(wallet)) {
            return true;
        }
        IdentityAccount account = authentication.getName() == null ? null
                : identityAccounts.findById(authentication.getName()).orElse(null);
        String userWallet = account == null ? null : account.walletAddress();
        String normalizedUser = userWallet == null ? null : WalletAddressRules.tryNormalizeWallet(userWallet).orElse(null);
        return normalizedUser != null && normalizedUser.equalsIgnoreCase(wallet);
    }

    private String resolveSessionWallet(HttpSession session, Authentication authentication) {
        String sessionWallet = WalletSession.currentWallet(session);
        if (!sessionWallet.isBlank()
                && WalletAddressRules.tryNormalizeWallet(sessionWallet).isPresent()) {
            return WalletAddressRules.tryNormalizeWallet(sessionWallet).orElse(null);
        }
        if (authentication != null && authentication.isAuthenticated()) {
            IdentityAccount account = authentication.getName() == null ? null
                    : identityAccounts.findById(authentication.getName()).orElse(null);
            if (account != null && account.walletAddress() != null
                    && WalletAddressRules.tryNormalizeWallet(account.walletAddress()).isPresent()) {
                return WalletAddressRules.tryNormalizeWallet(account.walletAddress()).orElse(null);
            }
        }
        return null;
    }

    public record SaveWalletSessionRequest(String walletAddress, int chainId, String signature, String message,
                                           String nonce) {
    }

    public record StakingTransactionRequest(String walletAddress, java.math.BigDecimal amount, String transactionHash) {
    }

    public record StakingClaimRequest(String walletAddress, String transactionHash) {
    }
}
