package dev.alexisreyna.artisanalbrew.web.api;

import dev.alexisreyna.artisanalbrew.application.config.ChainDefinition;
import dev.alexisreyna.artisanalbrew.application.config.ChainFamily;
import dev.alexisreyna.artisanalbrew.application.config.ChainRegistry;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.SolanaFaucetService;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.SolanaBase58;
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

import java.util.Map;

/** Port of {@code SolanaFaucetController} ({@code Web/Controllers}). */
@RestController
@RequestMapping("/faucet/api/solana")
public class SolanaFaucetController {
    private final SolanaFaucetService faucet;
    private final ChainRegistry chains;
    private final SelectedChainAccessor selectedChain;

    public SolanaFaucetController(SolanaFaucetService faucet, ChainRegistry chains,
                                  SelectedChainAccessor selectedChain) {
        this.faucet = faucet;
        this.chains = chains;
        this.selectedChain = selectedChain;
    }

    @GetMapping("/status")
    public ResponseEntity<?> status(@RequestParam String chainKey, @RequestParam String walletIdentifier,
                                    HttpSession session) {
        ChainDefinition chain = resolve(chainKey, walletIdentifier);
        if (chain == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "A valid enabled Solana chain and wallet are required."));
        }
        if (!WalletSession.matches(walletIdentifier.trim(), session, false)) {
            return ResponseEntity.status(401).body(Map.of("error", "Connect the wallet selected for this session."));
        }
        return ResponseEntity.ok(faucet.getStatus(chain.getKey(), walletIdentifier.trim()));
    }

    @PostMapping("/claim")
    public ResponseEntity<?> claim(@RequestBody FaucetClaimRequest request, HttpSession session) {
        ChainDefinition chain = resolve(request.chainKey(), request.walletIdentifier());
        if (chain == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "A valid enabled Solana chain and wallet are required."));
        }
        if (!selectedChain.selectedChainKey().equals(chain.getKey())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "The faucet chain does not match the selected chain."));
        }
        if (!WalletSession.matches(request.walletIdentifier().trim(), session, false)) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "The authenticated wallet does not match this claim."));
        }
        var result = faucet.claim(chain.getKey(), request.walletIdentifier().trim());
        return result.success() ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }

    private ChainDefinition resolve(String chainKey, String walletIdentifier) {
        ChainDefinition chain = chains.tryGet(chainKey)
                .filter(c -> c.isEnabled() && c.getFamily() == ChainFamily.SOLANA
                        && c.getCapabilities().faucet())
                .orElse(null);
        if (chain == null || walletIdentifier == null || !SolanaBase58.isPublicKey(walletIdentifier.trim())) {
            return null;
        }
        return chain;
    }

    public record FaucetClaimRequest(String chainKey, String walletIdentifier) {
    }
}
