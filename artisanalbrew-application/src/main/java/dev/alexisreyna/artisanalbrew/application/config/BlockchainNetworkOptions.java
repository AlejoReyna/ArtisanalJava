package dev.alexisreyna.artisanalbrew.application.config;

import java.math.BigDecimal;

/** Port of {@code BlockchainNetworkOptions} in {@code Configuration/BlockchainNetworkOptions.cs}. */
public final class BlockchainNetworkOptions {
    public static final String SECTION_NAME = "Blockchain:Network";
    public static final String LEGACY_SECTION_NAME = "Blockchain:BNBTestnet";

    private String networkName = "Ethereum Sepolia";
    private int chainId = 11155111;
    private String chainIdHex = "0xaa36a7";
    private String rpcUrl = "https://ethereum-sepolia-rpc.publicnode.com";
    private String serverRpcUrl;
    private String currencyName = "Sepolia ETH";
    private String currencySymbol = "ETH";
    private int currencyDecimals = 18;
    private String explorerUrl = "https://sepolia.etherscan.io/";
    private String paymentTokenContract = "";
    private String ankrBNBContract = "";
    private String stakingPoolContract = "";
    private String liquidVaultContract = "";
    private String coffeeCoinContract = "";
    private String cafeFaucetContract = "";
    private String marketplaceWallet = "";
    private BigDecimal stakingAprPercent = new BigDecimal("5.2");
    private int minimumConfirmations = 2;
    private long deploymentStartBlock;

    public String getNetworkName() { return networkName; }
    public void setNetworkName(String v) { this.networkName = v; }
    public int getChainId() { return chainId; }
    public void setChainId(int v) { this.chainId = v; }
    public String getChainIdHex() { return chainIdHex; }
    public void setChainIdHex(String v) { this.chainIdHex = v; }
    public String getRpcUrl() { return rpcUrl; }
    public void setRpcUrl(String v) { this.rpcUrl = v; }
    public String getServerRpcUrl() { return serverRpcUrl; }
    public void setServerRpcUrl(String v) { this.serverRpcUrl = v; }
    public String getCurrencyName() { return currencyName; }
    public void setCurrencyName(String v) { this.currencyName = v; }
    public String getCurrencySymbol() { return currencySymbol; }
    public void setCurrencySymbol(String v) { this.currencySymbol = v; }
    public int getCurrencyDecimals() { return currencyDecimals; }
    public void setCurrencyDecimals(int v) { this.currencyDecimals = v; }
    public String getExplorerUrl() { return explorerUrl; }
    public void setExplorerUrl(String v) { this.explorerUrl = v; }
    public String getPaymentTokenContract() { return paymentTokenContract; }
    public void setPaymentTokenContract(String v) { this.paymentTokenContract = v; }
    public String getAnkrBNBContract() { return ankrBNBContract; }
    public void setAnkrBNBContract(String v) { this.ankrBNBContract = v; }
    public String getStakingPoolContract() { return stakingPoolContract; }
    public void setStakingPoolContract(String v) { this.stakingPoolContract = v; }
    public String getLiquidVaultContract() { return liquidVaultContract; }
    public void setLiquidVaultContract(String v) { this.liquidVaultContract = v; }
    public String getCoffeeCoinContract() { return coffeeCoinContract; }
    public void setCoffeeCoinContract(String v) { this.coffeeCoinContract = v; }
    public String getCafeFaucetContract() { return cafeFaucetContract; }
    public void setCafeFaucetContract(String v) { this.cafeFaucetContract = v; }
    public String getMarketplaceWallet() { return marketplaceWallet; }
    public void setMarketplaceWallet(String v) { this.marketplaceWallet = v; }
    public BigDecimal getStakingAprPercent() { return stakingAprPercent; }
    public void setStakingAprPercent(BigDecimal v) { this.stakingAprPercent = v; }
    public int getMinimumConfirmations() { return minimumConfirmations; }
    public void setMinimumConfirmations(int v) { this.minimumConfirmations = v; }
    public long getDeploymentStartBlock() { return deploymentStartBlock; }
    public void setDeploymentStartBlock(long v) { this.deploymentStartBlock = v; }

    public String effectiveServerRpcUrl() {
        return serverRpcUrl == null || serverRpcUrl.isBlank() ? rpcUrl : serverRpcUrl;
    }

    public String effectivePaymentTokenContract() {
        return paymentTokenContract != null && !paymentTokenContract.isBlank() ? paymentTokenContract : ankrBNBContract;
    }
}
