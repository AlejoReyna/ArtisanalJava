package dev.alexisreyna.artisanalbrew.application.config;

/** Port of {@code Configuration/CoffeeCoinOwnerOptions.cs}. */
public final class CoffeeCoinOwnerOptions {
    public static final String SECTION_NAME = "CoffeeCoinOwner";

    private String privateKey;

    public String getPrivateKey() { return privateKey; }
    public void setPrivateKey(String v) { this.privateKey = v; }

    public boolean isConfigured() {
        return privateKey != null && !privateKey.isBlank();
    }
}
