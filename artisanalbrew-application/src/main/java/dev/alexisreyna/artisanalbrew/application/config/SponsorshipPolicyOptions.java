package dev.alexisreyna.artisanalbrew.application.config;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Port of {@code SponsorshipPolicyOptions} in {@code Configuration/SponsorshipPolicyOptions.cs}.
 * Fail-closed: disabled by default, empty allowlists deny everything.
 */
public final class SponsorshipPolicyOptions {
    public static final String SECTION_NAME = "Sponsorship";

    private boolean enabled;
    private List<String> allowedTargets = new ArrayList<>();
    private List<String> allowedSelectors = new ArrayList<>();
    private BigDecimal defaultBudgetUsd = new BigDecimal("10");
    private BigDecimal defaultMaxOperationCostUsd = new BigDecimal("1");
    private Duration defaultValidity = Duration.ofDays(30);
    private String verifyingSignerPrivateKey = "";
    private BigDecimal nativeCurrencyUsdRate = BigDecimal.ZERO;
    private long paymasterVerificationGasLimit = 500_000;
    private long paymasterPostOpGasLimit = 200_000;
    private Duration signatureValidity = Duration.ofMinutes(15);
    private int maxRevertedOperations = 5;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean v) { this.enabled = v; }
    public List<String> getAllowedTargets() { return allowedTargets; }
    public void setAllowedTargets(List<String> v) { this.allowedTargets = v; }
    public List<String> getAllowedSelectors() { return allowedSelectors; }
    public void setAllowedSelectors(List<String> v) { this.allowedSelectors = v; }
    public BigDecimal getDefaultBudgetUsd() { return defaultBudgetUsd; }
    public void setDefaultBudgetUsd(BigDecimal v) { this.defaultBudgetUsd = v; }
    public BigDecimal getDefaultMaxOperationCostUsd() { return defaultMaxOperationCostUsd; }
    public void setDefaultMaxOperationCostUsd(BigDecimal v) { this.defaultMaxOperationCostUsd = v; }
    public Duration getDefaultValidity() { return defaultValidity; }
    public void setDefaultValidity(Duration v) { this.defaultValidity = v; }
    public String getVerifyingSignerPrivateKey() { return verifyingSignerPrivateKey; }
    public void setVerifyingSignerPrivateKey(String v) { this.verifyingSignerPrivateKey = v; }
    public BigDecimal getNativeCurrencyUsdRate() { return nativeCurrencyUsdRate; }
    public void setNativeCurrencyUsdRate(BigDecimal v) { this.nativeCurrencyUsdRate = v; }
    public long getPaymasterVerificationGasLimit() { return paymasterVerificationGasLimit; }
    public void setPaymasterVerificationGasLimit(long v) { this.paymasterVerificationGasLimit = v; }
    public long getPaymasterPostOpGasLimit() { return paymasterPostOpGasLimit; }
    public void setPaymasterPostOpGasLimit(long v) { this.paymasterPostOpGasLimit = v; }
    public Duration getSignatureValidity() { return signatureValidity; }
    public void setSignatureValidity(Duration v) { this.signatureValidity = v; }
    public int getMaxRevertedOperations() { return maxRevertedOperations; }
    public void setMaxRevertedOperations(int v) { this.maxRevertedOperations = v; }

    public boolean canSign() {
        return enabled
                && verifyingSignerPrivateKey != null && !verifyingSignerPrivateKey.isBlank()
                && nativeCurrencyUsdRate != null && nativeCurrencyUsdRate.signum() > 0;
    }

    public boolean isTargetAllowed(String target) {
        return target != null && !target.isBlank()
                && allowedTargets.stream().anyMatch(t -> t.equalsIgnoreCase(target));
    }

    public boolean isSelectorAllowed(String selector) {
        return selector != null && !selector.isBlank()
                && allowedSelectors.stream().anyMatch(s -> s.equalsIgnoreCase(selector));
    }
}
