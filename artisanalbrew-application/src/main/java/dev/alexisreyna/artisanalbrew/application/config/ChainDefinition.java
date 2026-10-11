package dev.alexisreyna.artisanalbrew.application.config;

/** Port of {@code ChainDefinition} in {@code Configuration/ChainFamily.cs}. */
public final class ChainDefinition {
    private String key = "";
    private String displayName = "";
    private String shortName = "";
    private String iconAsset = "/images/eth_logo.png";
    private ChainFamily family;
    private boolean enabled = true;
    private int sortOrder;
    private Integer evmChainId;
    private String evmChainIdHex;
    private String solanaCluster;
    private String nativeCurrencyName = "";
    private String nativeCurrencySymbol = "";
    private int nativeCurrencyDecimals = 18;
    private String publicRpcUrl = "";
    private String serverRpcUrl;
    private String bundlerRpcUrl;
    private String modularBundlerRpcUrl;
    private String explorerAddressTemplate = "";
    private String explorerTransactionTemplate = "";
    private int minimumConfirmations = 2;
    private String solanaCommitment = "confirmed";
    private ChainDeployment deployment = new ChainDeployment();
    private ChainCapabilities capabilities = new ChainCapabilities();

    public String getKey() { return key; }
    public void setKey(String v) { this.key = v; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String v) { this.displayName = v; }
    public String getShortName() { return shortName; }
    public void setShortName(String v) { this.shortName = v; }
    public String getIconAsset() { return iconAsset; }
    public void setIconAsset(String v) { this.iconAsset = v; }
    public ChainFamily getFamily() { return family; }
    public void setFamily(ChainFamily v) { this.family = v; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean v) { this.enabled = v; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int v) { this.sortOrder = v; }
    public Integer getEvmChainId() { return evmChainId; }
    public void setEvmChainId(Integer v) { this.evmChainId = v; }
    public String getEvmChainIdHex() { return evmChainIdHex; }
    public void setEvmChainIdHex(String v) { this.evmChainIdHex = v; }
    public String getSolanaCluster() { return solanaCluster; }
    public void setSolanaCluster(String v) { this.solanaCluster = v; }
    public String getNativeCurrencyName() { return nativeCurrencyName; }
    public void setNativeCurrencyName(String v) { this.nativeCurrencyName = v; }
    public String getNativeCurrencySymbol() { return nativeCurrencySymbol; }
    public void setNativeCurrencySymbol(String v) { this.nativeCurrencySymbol = v; }
    public int getNativeCurrencyDecimals() { return nativeCurrencyDecimals; }
    public void setNativeCurrencyDecimals(int v) { this.nativeCurrencyDecimals = v; }
    public String getPublicRpcUrl() { return publicRpcUrl; }
    public void setPublicRpcUrl(String v) { this.publicRpcUrl = v; }
    public String getServerRpcUrl() { return serverRpcUrl; }
    public void setServerRpcUrl(String v) { this.serverRpcUrl = v; }
    public String getBundlerRpcUrl() { return bundlerRpcUrl; }
    public void setBundlerRpcUrl(String v) { this.bundlerRpcUrl = v; }
    public String getModularBundlerRpcUrl() { return modularBundlerRpcUrl; }
    public void setModularBundlerRpcUrl(String v) { this.modularBundlerRpcUrl = v; }
    public String getExplorerAddressTemplate() { return explorerAddressTemplate; }
    public void setExplorerAddressTemplate(String v) { this.explorerAddressTemplate = v; }
    public String getExplorerTransactionTemplate() { return explorerTransactionTemplate; }
    public void setExplorerTransactionTemplate(String v) { this.explorerTransactionTemplate = v; }
    public int getMinimumConfirmations() { return minimumConfirmations; }
    public void setMinimumConfirmations(int v) { this.minimumConfirmations = v; }
    public String getSolanaCommitment() { return solanaCommitment; }
    public void setSolanaCommitment(String v) { this.solanaCommitment = v; }
    public ChainDeployment getDeployment() { return deployment; }
    public void setDeployment(ChainDeployment v) { this.deployment = v; }
    public ChainCapabilities getCapabilities() { return capabilities; }
    public void setCapabilities(ChainCapabilities v) { this.capabilities = v; }

    public String effectiveServerRpcUrl() {
        return serverRpcUrl == null || serverRpcUrl.isBlank() ? publicRpcUrl : serverRpcUrl;
    }

    public String effectiveModularBundlerRpcUrl() {
        return modularBundlerRpcUrl == null || modularBundlerRpcUrl.isBlank() ? bundlerRpcUrl : modularBundlerRpcUrl;
    }

    public String familyName() {
        return family == null ? "" : family.name();
    }
}
