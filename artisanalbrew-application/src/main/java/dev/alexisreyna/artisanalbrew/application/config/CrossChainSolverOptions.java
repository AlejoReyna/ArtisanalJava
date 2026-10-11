package dev.alexisreyna.artisanalbrew.application.config;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Port of {@code CrossChainSolverOptions} in {@code Configuration/CrossChainSolverOptions.cs}.
 * Fail-closed: disabled by default, empty allowlists deny every intent.
 */
public final class CrossChainSolverOptions {
    public static final String SECTION_NAME = "CrossChainSolver";

    private boolean enabled;
    private String sourceChainKey = "";
    private String sourceResolverAddress = "";
    private String destinationChainKey = "";
    private String destinationResolverAddress = "";
    private String solverPrivateKey = "";
    private List<TokenPair> allowedTokenPairs = new ArrayList<>();
    private BigDecimal maxAmountIn = BigDecimal.ZERO;
    private int maxOutputBps = 10000;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean v) { this.enabled = v; }
    public String getSourceChainKey() { return sourceChainKey; }
    public void setSourceChainKey(String v) { this.sourceChainKey = v; }
    public String getSourceResolverAddress() { return sourceResolverAddress; }
    public void setSourceResolverAddress(String v) { this.sourceResolverAddress = v; }
    public String getDestinationChainKey() { return destinationChainKey; }
    public void setDestinationChainKey(String v) { this.destinationChainKey = v; }
    public String getDestinationResolverAddress() { return destinationResolverAddress; }
    public void setDestinationResolverAddress(String v) { this.destinationResolverAddress = v; }
    public String getSolverPrivateKey() { return solverPrivateKey; }
    public void setSolverPrivateKey(String v) { this.solverPrivateKey = v; }
    public List<TokenPair> getAllowedTokenPairs() { return allowedTokenPairs; }
    public void setAllowedTokenPairs(List<TokenPair> v) { this.allowedTokenPairs = v; }
    public BigDecimal getMaxAmountIn() { return maxAmountIn; }
    public void setMaxAmountIn(BigDecimal v) { this.maxAmountIn = v; }
    public int getMaxOutputBps() { return maxOutputBps; }
    public void setMaxOutputBps(int v) { this.maxOutputBps = v; }

    public boolean canOperate() {
        return canPrice()
                && !isBlank(sourceResolverAddress)
                && !isBlank(destinationResolverAddress)
                && !isBlank(solverPrivateKey);
    }

    public boolean canPrice() {
        return enabled && !isBlank(sourceChainKey) && !isBlank(destinationChainKey);
    }

    public boolean isTokenPairAllowed(String sourceToken, String destinationToken) {
        return allowedTokenPairs.stream().anyMatch(p ->
                p.sourceToken().equalsIgnoreCase(sourceToken)
                        && p.destinationToken().equalsIgnoreCase(destinationToken));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record TokenPair(String sourceToken, String destinationToken) {
    }
}
