package dev.alexisreyna.artisanalbrew.infrastructure.security;

import dev.alexisreyna.artisanalbrew.application.repository.WalletIdentityRepository;
import dev.alexisreyna.artisanalbrew.application.service.IdentityAccount;
import dev.alexisreyna.artisanalbrew.application.service.IdentityAccountService;
import dev.alexisreyna.artisanalbrew.application.service.IdentityOperationResult;
import dev.alexisreyna.artisanalbrew.application.service.WalletAccountResult;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.ApplicationUserEntity;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.ApplicationUserJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.RoleJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.UserRoleJpaRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Port of {@code IdentityAccountService} ({@code Infrastructure/Identity}).
 * {@code UserManager}/{@code SignInManager} mechanics are reimplemented
 * directly: normalized lookups, PBKDF2/BCrypt verification, and the
 * 5-failures → 15-minute lockout from {@code Program.cs}.
 */
@Service
@Transactional
public class JpaIdentityAccountService implements IdentityAccountService {
    static final int MAX_FAILED_ATTEMPTS = 5;
    static final Duration LOCKOUT_DURATION = Duration.ofMinutes(15);

    private final ApplicationUserJpaRepository users;
    private final UserRoleJpaRepository userRoles;
    private final RoleJpaRepository roles;
    private final WalletIdentityRepository walletIdentities;
    private final PasswordEncoder passwords;

    public JpaIdentityAccountService(ApplicationUserJpaRepository users, UserRoleJpaRepository userRoles,
                                     RoleJpaRepository roles, WalletIdentityRepository walletIdentities,
                                     PasswordEncoder passwords) {
        this.users = users;
        this.userRoles = userRoles;
        this.roles = roles;
        this.walletIdentities = walletIdentities;
        this.passwords = passwords;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IdentityAccount> findById(String accountId) {
        return users.findById(accountId).map(JpaIdentityAccountService::toAccount);
    }

    @Override
    public WalletAccountResult findOrCreateAndLinkWallet(String walletAddress, Integer walletChainId,
                                                         String walletProvider, String walletFamily) {
        boolean isEvm = "evm".equalsIgnoreCase(walletFamily);
        String normalizedAddress = isEvm ? walletAddress.toLowerCase(Locale.ROOT) : walletAddress;
        var identity = walletIdentities.find(walletFamily, normalizedAddress);
        ApplicationUserEntity user = null;
        if (identity.isPresent()) {
            user = users.findById(identity.get().getUserId()).orElseThrow(
                    () -> new IllegalStateException("The wallet identity points to a missing user."));
        }
        if (user == null) {
            user = isEvm
                    ? users.findFirstByWalletAddressIgnoreCase(normalizedAddress).orElse(null)
                    : users.findByWalletAddress(walletAddress).orElse(null);
        }
        if (user == null) {
            user = users.findByNormalizedUserName(normalize(createUserName(walletAddress, isEvm))).orElse(null);
        }
        if (user == null) {
            user = users.findByNormalizedEmail(normalize(createSyntheticEmail(walletAddress, isEvm))).orElse(null);
        }
        if (user == null) {
            user = new ApplicationUserEntity();
            user.id = UUID.randomUUID().toString();
            user.userName = createUserName(walletAddress, isEvm);
            user.normalizedUserName = normalize(user.userName);
            user.email = createSyntheticEmail(walletAddress, isEvm);
            user.normalizedEmail = normalize(user.email);
            user.emailConfirmed = true;
            user.securityStamp = UUID.randomUUID().toString();
            users.save(user);
        }
        user.walletAddress = walletAddress;
        user.walletChainId = walletChainId;
        user.walletVerifiedAt = Instant.now();
        users.save(user);

        boolean linked = walletIdentities.upsert(user.id, walletFamily, normalizedAddress, walletAddress, walletProvider);
        IdentityAccount account = toAccount(user);
        return linked
                ? new WalletAccountResult(true, account, false, null)
                : new WalletAccountResult(false, account, true, null);
    }

    @Override
    public IdentityOperationResult delete(String accountId) {
        Optional<ApplicationUserEntity> user = users.findById(accountId);
        if (user.isEmpty()) {
            return new IdentityOperationResult(false, "Your session has expired. Please log in again.");
        }
        userRoles.deleteByUserId(accountId);
        users.deleteById(accountId);
        return new IdentityOperationResult(true, null);
    }

    @Override
    public boolean passwordSignIn(String email, String password, boolean persistent) {
        Optional<ApplicationUserEntity> user =
                users.findByNormalizedEmail(email == null ? "" : email.toUpperCase(Locale.ROOT));
        if (user.isEmpty()) {
            return false;
        }
        ApplicationUserEntity account = user.get();
        if (account.lockoutEnd != null && account.lockoutEnd.isAfter(Instant.now())) {
            return false;
        }
        if (account.passwordHash == null || !passwords.matches(password, account.passwordHash)) {
            int failures = account.accessFailedCount + 1;
            account.accessFailedCount = failures;
            if (failures >= MAX_FAILED_ATTEMPTS) {
                account.lockoutEnd = Instant.now().plus(LOCKOUT_DURATION);
            }
            users.save(account);
            return false;
        }
        account.accessFailedCount = 0;
        account.lockoutEnd = null;
        users.save(account);
        signIn(account.id, persistent);
        return true;
    }

    @Override
    public void signIn(String accountId, boolean persistent) {
        ApplicationUserEntity user = users.findById(accountId).orElseThrow(
                () -> new IllegalStateException("The wallet identity points to a missing user."));
        List<SimpleGrantedAuthority> authorities = userRoles.findByUserId(user.id).stream()
                .map(link -> roles.findById(link.roleId).map(ApplicationUserDetailsService::toAuthority).orElse(null))
                .filter(a -> a != null)
                .toList();
        WalletAuthentication authentication = new WalletAuthentication(user.id, authorities, Map.of(
                "wallet_address", user.walletAddress == null ? "" : user.walletAddress,
                "wallet_chain_id", user.walletChainId == null ? "" : user.walletChainId.toString()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Override
    public void signOut() {
        SecurityContextHolder.clearContext();
    }

    static IdentityAccount toAccount(ApplicationUserEntity user) {
        return new IdentityAccount(user.id, user.userProfileId, user.walletAddress);
    }

    static String normalize(String value) {
        return value == null ? "" : value.toUpperCase(Locale.ROOT);
    }

    static String createUserName(String address, boolean isEvm) {
        return isEvm ? address : "solana" + address;
    }

    static String createSyntheticEmail(String address, boolean isEvm) {
        if (isEvm) {
            return address.toLowerCase(Locale.ROOT) + "@wallet.thiscafeteria.local";
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(address.getBytes(StandardCharsets.UTF_8));
            return "solana-" + HexFormat.of().formatHex(digest).toLowerCase(Locale.ROOT) + "@wallet.invalid";
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
