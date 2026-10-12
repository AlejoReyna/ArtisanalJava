package dev.alexisreyna.artisanalbrew.web.auth;

import dev.alexisreyna.artisanalbrew.application.service.blockchain.WalletAddressRules;
import dev.alexisreyna.artisanalbrew.infrastructure.security.WalletAuthentication;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Resolves "the connected wallet" with the .NET candidate order:
 * authentication {@code wallet_address} detail, then the session.
 * EVM matching is case-insensitive (checksum); Solana is exact.
 */
public final class WalletSession {
    public static final String WALLET_ADDRESS = "WalletAddress";
    public static final String WALLET_CHAIN_KEY = "WalletChainKey";

    private WalletSession() {
    }

    public static String currentWallet(HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof WalletAuthentication wallet && wallet.walletAddress() != null
                && !wallet.walletAddress().isBlank()) {
            return wallet.walletAddress();
        }
        if (authentication != null && authentication.getName() != null
                && WalletAddressRules.tryNormalizeWallet(authentication.getName()).isPresent()) {
            return authentication.getName();
        }
        Object stored = session == null ? null : session.getAttribute(WALLET_ADDRESS);
        return stored == null ? "" : stored.toString();
    }

    public static boolean matches(String wallet, HttpSession session, boolean evm) {
        String current = currentWallet(session);
        if (current.isBlank()) {
            return false;
        }
        return evm ? current.equalsIgnoreCase(wallet) : current.equals(wallet);
    }
}
