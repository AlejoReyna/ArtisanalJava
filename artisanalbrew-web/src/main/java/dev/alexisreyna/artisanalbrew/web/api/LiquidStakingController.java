package dev.alexisreyna.artisanalbrew.web.api;

import dev.alexisreyna.artisanalbrew.application.config.ChainDefinition;
import dev.alexisreyna.artisanalbrew.application.config.ChainFamily;
import dev.alexisreyna.artisanalbrew.application.config.ChainRegistry;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.LiquidStakingGateway;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.LiquidStakingLedgerService;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.LiquidStakingOperation;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.LiquidTransactionVerificationResult;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.SolanaBase58;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.TransactionVerificationStatus;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.WalletAddressRules;
import dev.alexisreyna.artisanalbrew.domain.entities.StakingLedgerEntry;
import dev.alexisreyna.artisanalbrew.web.auth.WalletSession;
import dev.alexisreyna.artisanalbrew.web.chain.SelectedChainAccessor;
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

/** Port of {@code LiquidStakingController} ({@code Web/Controllers}). */
@RestController
@RequestMapping("/staking/api/liquid")
public class LiquidStakingController {
    private final LiquidStakingGateway gateway;
    private final LiquidStakingLedgerService ledger;
    private final ChainRegistry chains;
    private final SelectedChainAccessor selectedChain;

    public LiquidStakingController(LiquidStakingGateway gateway, LiquidStakingLedgerService ledger,
                                   ChainRegistry chains, SelectedChainAccessor selectedChain) {
        this.gateway = gateway;
        this.ledger = ledger;
        this.chains = chains;
        this.selectedChain = selectedChain;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(@RequestParam String chainKey, @RequestParam String walletIdentifier,
                                       HttpSession session) {
        ChainDefinition chain = enabledChain(chainKey);
        String wallet = normalizeWallet(chain, walletIdentifier);
        if (chain == null || wallet == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "A valid enabled chain and wallet are required."));
        }
        if (!WalletSession.matches(wallet, session, chain.getFamily() == ChainFamily.EVM)) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Connect the wallet selected for this session."));
        }
        return ResponseEntity.ok(gateway.getDashboard(chainKey, wallet));
    }

    @PostMapping("/record-deposit")
    public ResponseEntity<?> recordDeposit(@RequestBody LiquidTransactionRequest request, HttpSession session) {
        return record(request, LiquidStakingOperation.DEPOSIT, session);
    }

    @PostMapping("/record-redeem")
    public ResponseEntity<?> recordRedeem(@RequestBody LiquidTransactionRequest request, HttpSession session) {
        return record(request, LiquidStakingOperation.REDEEM, session);
    }

    @PostMapping("/record-claim")
    public ResponseEntity<?> recordClaim(@RequestBody LiquidTransactionRequest request, HttpSession session) {
        return record(request, LiquidStakingOperation.CLAIM, session);
    }

    @PostMapping("/record-reward-funding")
    public ResponseEntity<?> recordRewardFunding(@RequestBody LiquidTransactionRequest request, HttpSession session) {
        return record(request, LiquidStakingOperation.REWARD_FUNDING, session);
    }

    private ResponseEntity<?> record(LiquidTransactionRequest request, LiquidStakingOperation operation,
                                     HttpSession session) {
        ChainDefinition chain = enabledChain(request.chainKey());
        String wallet = chain == null ? null : normalizeWallet(chain, request.walletIdentifier());
        if (chain == null || wallet == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "A valid enabled chain and wallet are required."));
        }
        if (!selectedChain.selectedChainKey().equals(chain.getKey())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "The transaction chain does not match the selected chain."));
        }
        if (!WalletSession.matches(wallet, session, chain.getFamily() == ChainFamily.EVM)) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "The authenticated wallet does not match this transaction."));
        }
        String transactionId = normalizeTransactionId(chain, request.transactionId());
        if (transactionId == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "A valid transaction identifier is required."));
        }
        var result = ledger.record(chain, wallet, transactionId, operation, request.expectedAmount());
        return switch (result.status()) {
            case RECORDED, ALREADY_RECORDED -> {
                StakingLedgerEntry entry = result.entry();
                yield ResponseEntity.ok(Map.of("success", true,
                        "chainKey", entry.getChainKey(),
                        "transactionHash", entry.getTransactionHash(),
                        "operationIndex", entry.getOperationIndex(),
                        "actionType", entry.getActionType(),
                        "explorerUrl", entry.getExplorerUrl() == null ? "" : entry.getExplorerUrl()));
            }
            case PENDING_CONFIRMATIONS -> ResponseEntity.status(202).body(Map.of(
                    "status", "pending_confirmations", "confirmations", 0,
                    "requiredConfirmations", chain.getMinimumConfirmations()));
            case VERIFICATION_FAILED -> ResponseEntity.badRequest().body(Map.of("error",
                    result.error() == null ? "Liquid-staking transaction could not be verified." : result.error()));
            default -> ResponseEntity.status(409)
                    .body(Map.of("error", "The verified operation could not be recorded safely."));
        };
    }

    private ChainDefinition enabledChain(String chainKey) {
        return chains.tryGet(chainKey).filter(ChainDefinition::isEnabled).orElse(null);
    }

    private static String normalizeWallet(ChainDefinition chain, String value) {
        if (value == null) {
            return null;
        }
        if (chain.getFamily() == ChainFamily.EVM) {
            return WalletAddressRules.tryNormalizeWallet(value).orElse(null);
        }
        String wallet = value.trim();
        return SolanaBase58.isPublicKey(wallet) ? wallet : null;
    }

    private static String normalizeTransactionId(ChainDefinition chain, String value) {
        if (value == null) {
            return null;
        }
        String transactionId = value.trim();
        if (chain.getFamily() == ChainFamily.EVM) {
            return WalletAddressRules.tryNormalizeTransactionHash(transactionId).orElse(null);
        }
        byte[] signature = SolanaBase58.tryDecode(transactionId);
        return signature != null && signature.length == 64 ? transactionId : null;
    }

    public record LiquidTransactionRequest(String chainKey, String walletIdentifier, String transactionId,
                                           BigDecimal expectedAmount) {
    }
}
