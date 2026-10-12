package dev.alexisreyna.artisanalbrew.web.api;

import dev.alexisreyna.artisanalbrew.application.config.BlockchainNetworkOptions;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.WalletAddressRules;
import dev.alexisreyna.artisanalbrew.application.service.rewards.LoyaltyMintService;
import dev.alexisreyna.artisanalbrew.application.service.rewards.RewardClaimService;
import dev.alexisreyna.artisanalbrew.application.service.rewards.LoyaltyMintCommand;
import dev.alexisreyna.artisanalbrew.web.auth.WalletSession;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

/** Port of {@code RewardsController} ({@code Web/Controllers}, {@code rewards/*}). */
@RestController
@RequestMapping("/rewards")
public class RewardsController {
    private final RewardClaimService rewards;
    private final LoyaltyMintService loyalty;
    private final BlockchainNetworkOptions chain;

    public RewardsController(RewardClaimService rewards, LoyaltyMintService loyalty,
                             BlockchainNetworkOptions chain) {
        this.rewards = rewards;
        this.loyalty = loyalty;
        this.chain = chain;
    }

    @GetMapping("/api/claimable")
    public ResponseEntity<?> claimable(@RequestParam String walletAddress) {
        String wallet = WalletAddressRules.tryNormalizeWallet(walletAddress).orElse(null);
        if (wallet == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "A valid wallet address is required."));
        }
        return ResponseEntity.ok(rewards.getClaimStatus(wallet));
    }

    @PostMapping("/api/claim")
    public ResponseEntity<?> claim(@RequestBody WalletRequest request, HttpSession session) {
        String wallet = WalletAddressRules.tryNormalizeWallet(request.walletAddress()).orElse(null);
        if (wallet == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "A valid wallet address is required."));
        }
        String sessionWallet = WalletSession.currentWallet(session);
        if (sessionWallet.isBlank()) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Connect or sign in with your wallet before claiming rewards."));
        }
        if (!sessionWallet.equalsIgnoreCase(wallet)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "The connected wallet does not match this claim."));
        }
        var result = rewards.claimDailyReward(wallet);
        return result.isSuccess() ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }

    @PostMapping("/api/mint-loyalty")
    public ResponseEntity<?> mintLoyalty(@RequestBody MintLoyaltyRequest request, HttpSession session) {
        String wallet = WalletAddressRules.tryNormalizeWallet(request.walletAddress()).orElse(null);
        if (wallet == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "A valid wallet address is required."));
        }
        String sessionWallet = WalletSession.currentWallet(session);
        if (sessionWallet.isBlank()) {
            return ResponseEntity.status(401).body(
                    Map.of("error", "Connect or sign in with your wallet before minting allocation rewards."));
        }
        if (!sessionWallet.equalsIgnoreCase(wallet)) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "The connected wallet does not match the allocation session wallet."));
        }
        if (request.amount() == null || request.amount().signum() <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Loyalty reward amount must be greater than zero."));
        }
        if (request.paymentAmount() == null || request.paymentAmount().signum() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Payment amount must be greater than zero."));
        }
        String paymentHash = WalletAddressRules.tryNormalizeTransactionHash(request.paymentTransactionHash())
                .orElse(null);
        if (paymentHash == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "A valid payment token transaction hash is required."));
        }
        var result = loyalty.mint(chain, new LoyaltyMintCommand(wallet, request.amount(),
                request.paymentAmount(), paymentHash, request.allocationName()));
        return switch (result.status()) {
            case MINTED -> ResponseEntity.ok(Map.of("success", true,
                    "transactionHash", result.mintTransactionHash(),
                    "paymentTransactionHash", paymentHash,
                    "mintedAmount", request.amount()));
            case ALREADY_CLAIMED -> ResponseEntity.status(409)
                    .body(Map.of("error", "Esta transacción ya ha sido reclamada."));
            case PENDING_CONFIRMATIONS -> ResponseEntity.status(202).body(Map.of(
                    "success", false, "status", "pending_confirmations"));
            case MINTING_NOT_CONFIGURED -> ResponseEntity.badRequest()
                    .body(Map.of("error", "Minting is not configured on the server."));
            case MINT_FAILED -> ResponseEntity.badRequest().body(Map.of("error", result.error()));
            default -> ResponseEntity.badRequest().body(Map.of("error",
                    "Payment transaction could not be verified on-chain. It must be a successful configured "
                            + "ERC-20 payment token transfer from your wallet to the configured marketplace "
                            + "wallet for the exact coffee price."));
        };
    }

    public record WalletRequest(String walletAddress) {
    }

    public record MintLoyaltyRequest(String walletAddress, BigDecimal amount, BigDecimal paymentAmount,
                                     String paymentTransactionHash, String allocationName) {
    }
}
