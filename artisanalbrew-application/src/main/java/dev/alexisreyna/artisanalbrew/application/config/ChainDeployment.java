package dev.alexisreyna.artisanalbrew.application.config;

/**
 * Port of {@code ChainDeployment} in {@code Configuration/ChainFamily.cs}.
 * Mutable POJO (binds from YAML/manifest in later phases); records would force
 * 40-arg constructors.
 */
public final class ChainDeployment {
    private String cafe = "";
    private String coffee = "";
    private String stCafe = "";
    private String liquidVault = "";
    private String legacyPool = "";
    private String faucet = "";
    private String agenticEscrow = "";
    private String paymentToken = "";
    private String entryPoint = "";
    private String accountFactory = "";
    private String verifyingPaymaster = "";
    private String erc8004Registry = "";
    private String erc7683Resolver = "";
    private String modularEntryPoint = "";
    private String modularAccountFactory = "";
    private String delegationManager = "";
    private String hybridDeleGatorImplementation = "";
    private String allowedTargetsEnforcer = "";
    private String allowedMethodsEnforcer = "";
    private String exactCalldataEnforcer = "";
    private String limitedCallsEnforcer = "";
    private String nonceEnforcer = "";
    private String timestampEnforcer = "";
    private String modularAccountType = "";
    private String modularFrameworkRevision = "";
    private String program = "";
    private String vaultPda = "";
    private String authorityPda = "";
    private String cafeCustody = "";
    private String coffeeCustody = "";
    private String admin = "";
    private String tokenProgram = "";
    private String token2022Program = "";
    private int cafeDecimals = 18;
    private int stCafeDecimals = 18;
    private int coffeeDecimals = 18;
    private long startBlockOrSlot;

    public String getCafe() { return cafe; }
    public void setCafe(String v) { this.cafe = v; }
    public String getCoffee() { return coffee; }
    public void setCoffee(String v) { this.coffee = v; }
    public String getStCafe() { return stCafe; }
    public void setStCafe(String v) { this.stCafe = v; }
    public String getLiquidVault() { return liquidVault; }
    public void setLiquidVault(String v) { this.liquidVault = v; }
    public String getLegacyPool() { return legacyPool; }
    public void setLegacyPool(String v) { this.legacyPool = v; }
    public String getFaucet() { return faucet; }
    public void setFaucet(String v) { this.faucet = v; }
    public String getAgenticEscrow() { return agenticEscrow; }
    public void setAgenticEscrow(String v) { this.agenticEscrow = v; }
    public String getPaymentToken() { return paymentToken; }
    public void setPaymentToken(String v) { this.paymentToken = v; }
    public String getEntryPoint() { return entryPoint; }
    public void setEntryPoint(String v) { this.entryPoint = v; }
    public String getAccountFactory() { return accountFactory; }
    public void setAccountFactory(String v) { this.accountFactory = v; }
    public String getVerifyingPaymaster() { return verifyingPaymaster; }
    public void setVerifyingPaymaster(String v) { this.verifyingPaymaster = v; }
    public String getErc8004Registry() { return erc8004Registry; }
    public void setErc8004Registry(String v) { this.erc8004Registry = v; }
    public String getErc7683Resolver() { return erc7683Resolver; }
    public void setErc7683Resolver(String v) { this.erc7683Resolver = v; }
    public String getModularEntryPoint() { return modularEntryPoint; }
    public void setModularEntryPoint(String v) { this.modularEntryPoint = v; }
    public String getModularAccountFactory() { return modularAccountFactory; }
    public void setModularAccountFactory(String v) { this.modularAccountFactory = v; }
    public String getDelegationManager() { return delegationManager; }
    public void setDelegationManager(String v) { this.delegationManager = v; }
    public String getHybridDeleGatorImplementation() { return hybridDeleGatorImplementation; }
    public void setHybridDeleGatorImplementation(String v) { this.hybridDeleGatorImplementation = v; }
    public String getAllowedTargetsEnforcer() { return allowedTargetsEnforcer; }
    public void setAllowedTargetsEnforcer(String v) { this.allowedTargetsEnforcer = v; }
    public String getAllowedMethodsEnforcer() { return allowedMethodsEnforcer; }
    public void setAllowedMethodsEnforcer(String v) { this.allowedMethodsEnforcer = v; }
    public String getExactCalldataEnforcer() { return exactCalldataEnforcer; }
    public void setExactCalldataEnforcer(String v) { this.exactCalldataEnforcer = v; }
    public String getLimitedCallsEnforcer() { return limitedCallsEnforcer; }
    public void setLimitedCallsEnforcer(String v) { this.limitedCallsEnforcer = v; }
    public String getNonceEnforcer() { return nonceEnforcer; }
    public void setNonceEnforcer(String v) { this.nonceEnforcer = v; }
    public String getTimestampEnforcer() { return timestampEnforcer; }
    public void setTimestampEnforcer(String v) { this.timestampEnforcer = v; }
    public String getModularAccountType() { return modularAccountType; }
    public void setModularAccountType(String v) { this.modularAccountType = v; }
    public String getModularFrameworkRevision() { return modularFrameworkRevision; }
    public void setModularFrameworkRevision(String v) { this.modularFrameworkRevision = v; }
    public String getProgram() { return program; }
    public void setProgram(String v) { this.program = v; }
    public String getVaultPda() { return vaultPda; }
    public void setVaultPda(String v) { this.vaultPda = v; }
    public String getAuthorityPda() { return authorityPda; }
    public void setAuthorityPda(String v) { this.authorityPda = v; }
    public String getCafeCustody() { return cafeCustody; }
    public void setCafeCustody(String v) { this.cafeCustody = v; }
    public String getCoffeeCustody() { return coffeeCustody; }
    public void setCoffeeCustody(String v) { this.coffeeCustody = v; }
    public String getAdmin() { return admin; }
    public void setAdmin(String v) { this.admin = v; }
    public String getTokenProgram() { return tokenProgram; }
    public void setTokenProgram(String v) { this.tokenProgram = v; }
    public String getToken2022Program() { return token2022Program; }
    public void setToken2022Program(String v) { this.token2022Program = v; }
    public int getCafeDecimals() { return cafeDecimals; }
    public void setCafeDecimals(int v) { this.cafeDecimals = v; }
    public int getStCafeDecimals() { return stCafeDecimals; }
    public void setStCafeDecimals(int v) { this.stCafeDecimals = v; }
    public int getCoffeeDecimals() { return coffeeDecimals; }
    public void setCoffeeDecimals(int v) { this.coffeeDecimals = v; }
    public long getStartBlockOrSlot() { return startBlockOrSlot; }
    public void setStartBlockOrSlot(long v) { this.startBlockOrSlot = v; }
}
