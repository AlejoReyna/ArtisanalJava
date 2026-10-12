package dev.alexisreyna.artisanalbrew.infrastructure.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Authenticated wallet or account session (mirrors the cookie identity
 * {@code WalletAuthController} mints: NameIdentifier/Name claims plus
 * {@code wallet_address}, {@code wallet_chain_id}, {@code chain_key}).
 */
public final class WalletAuthentication extends AbstractAuthenticationToken {
    private final String accountId;
    private final Map<String, String> walletDetails;

    public WalletAuthentication(String accountId, Collection<? extends GrantedAuthority> authorities,
                                Map<String, String> walletDetails) {
        super(authorities);
        this.accountId = accountId;
        this.walletDetails = walletDetails == null ? Map.of() : Map.copyOf(walletDetails);
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return accountId;
    }

    @Override
    public String getName() {
        return accountId;
    }

    public Map<String, String> getWalletDetails() {
        return Collections.unmodifiableMap(walletDetails);
    }

    public String walletAddress() {
        return walletDetails.get("wallet_address");
    }
}
