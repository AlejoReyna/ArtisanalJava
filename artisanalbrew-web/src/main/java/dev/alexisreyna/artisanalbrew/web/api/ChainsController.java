package dev.alexisreyna.artisanalbrew.web.api;

import dev.alexisreyna.artisanalbrew.application.config.ChainDefinition;
import dev.alexisreyna.artisanalbrew.application.config.ChainRegistry;
import dev.alexisreyna.artisanalbrew.web.chain.SelectedChainAccessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Port of {@code ChainsController} ({@code Web/Controllers}). Only enabled
 * chains are listed, and server-only URLs (server/bundler RPC) never leave
 * the server — exactly like .NET.
 */
@RestController
@RequestMapping("/api/chains")
public class ChainsController {
    private final ChainRegistry registry;
    private final SelectedChainAccessor selected;

    public ChainsController(ChainRegistry registry, SelectedChainAccessor selected) {
        this.registry = registry;
        this.selected = selected;
    }

    @GetMapping
    public Map<String, Object> chains() {
        List<Map<String, Object>> chains = registry.all().stream()
                .filter(ChainDefinition::isEnabled)
                .map(ChainsController::describe)
                .toList();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("defaultChainKey", registry.defaultChainKey());
        body.put("selectedChainKey", selected.selectedChainKey());
        body.put("chains", chains);
        return body;
    }

    @PostMapping("/select")
    public ResponseEntity<?> select(@RequestBody SelectChainRequest request) {
        String key = request.chainKey() == null ? "" : request.chainKey().trim();
        if (key.isEmpty() || !selected.select(key)) {
            return ResponseEntity.badRequest().body(Map.of("error", "The requested chain is unknown or disabled."));
        }
        ChainDefinition chain = selected.selectedChain();
        return ResponseEntity.ok(Map.of("selectedChainKey", chain.getKey(), "family", chain.familyName()));
    }

    static Map<String, Object> describe(ChainDefinition chain) {
        Map<String, Object> nativeCurrency = new LinkedHashMap<>();
        nativeCurrency.put("name", chain.getNativeCurrencyName());
        nativeCurrency.put("symbol", chain.getNativeCurrencySymbol());
        nativeCurrency.put("decimals", chain.getNativeCurrencyDecimals());
        Map<String, Object> deployments = new LinkedHashMap<>();
        deployments.put("cafe", chain.getDeployment().getCafe());
        deployments.put("coffee", chain.getDeployment().getCoffee());
        deployments.put("stCafe", chain.getDeployment().getStCafe());
        deployments.put("liquidVault", chain.getDeployment().getLiquidVault());
        deployments.put("legacyPool", chain.getDeployment().getLegacyPool());
        deployments.put("faucet", chain.getDeployment().getFaucet());
        deployments.put("program", chain.getDeployment().getProgram());
        deployments.put("vaultPda", chain.getDeployment().getVaultPda());
        deployments.put("authorityPda", chain.getDeployment().getAuthorityPda());
        deployments.put("cafeCustody", chain.getDeployment().getCafeCustody());
        deployments.put("coffeeCustody", chain.getDeployment().getCoffeeCustody());
        deployments.put("admin", chain.getDeployment().getAdmin());
        deployments.put("tokenProgram", chain.getDeployment().getTokenProgram());
        deployments.put("token2022Program", chain.getDeployment().getToken2022Program());
        deployments.put("cafeDecimals", chain.getDeployment().getCafeDecimals());
        deployments.put("stCafeDecimals", chain.getDeployment().getStCafeDecimals());
        deployments.put("coffeeDecimals", chain.getDeployment().getCoffeeDecimals());
        deployments.put("startBlockOrSlot", chain.getDeployment().getStartBlockOrSlot());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("key", chain.getKey());
        body.put("name", chain.getDisplayName());
        body.put("shortName", chain.getShortName());
        body.put("iconAsset", chain.getIconAsset());
        body.put("family", chain.familyName());
        body.put("chainId", chain.getEvmChainId());
        body.put("chainIdHex", chain.getEvmChainIdHex());
        body.put("cluster", chain.getSolanaCluster());
        body.put("nativeCurrency", nativeCurrency);
        body.put("publicRpcUrl", chain.getPublicRpcUrl());
        body.put("explorerAddressTemplate", chain.getExplorerAddressTemplate());
        body.put("explorerTransactionTemplate", chain.getExplorerTransactionTemplate());
        body.put("minimumConfirmations", chain.getMinimumConfirmations());
        body.put("solanaCommitment", chain.getSolanaCommitment());
        body.put("capabilities", chain.getCapabilities());
        body.put("deployments", deployments);
        return body;
    }

    public record SelectChainRequest(String chainKey) {
    }
}
