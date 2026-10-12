package dev.alexisreyna.artisanalbrew.infrastructure.persistence.adapter;

import dev.alexisreyna.artisanalbrew.application.repository.WalletIdentityRepository;
import dev.alexisreyna.artisanalbrew.domain.entities.WalletIdentity;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.WalletIdentityEntity;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.WalletIdentityJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/** JPA adapter for {@link WalletIdentityRepository}. */
@Repository
@Transactional
public class JpaWalletIdentityRepositoryAdapter implements WalletIdentityRepository {
    private final WalletIdentityJpaRepository jpa;

    public JpaWalletIdentityRepositoryAdapter(WalletIdentityJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WalletIdentity> find(String family, String normalizedAddress) {
        return jpa.findByFamilyAndNormalizedAddress(family, normalizedAddress)
                .map(JpaWalletIdentityRepositoryAdapter::toDomain);
    }

    @Override
    public boolean upsert(String userId, String family, String normalizedAddress,
                          String displayAddress, String walletProvider) {
        Optional<WalletIdentityEntity> existing =
                jpa.findByFamilyAndNormalizedAddress(family, normalizedAddress);
        if (existing.isEmpty()) {
            WalletIdentityEntity e = new WalletIdentityEntity();
            e.id = java.util.UUID.randomUUID();
            e.userId = userId;
            e.family = family;
            e.normalizedAddress = normalizedAddress;
            e.displayAddress = displayAddress;
            e.walletProvider = walletProvider == null || walletProvider.isBlank() ? "unknown" : walletProvider;
            e.verifiedAtUtc = Instant.now();
            jpa.save(e);
            return true;
        }
        WalletIdentityEntity e = existing.get();
        if (!e.userId.equals(userId)) {
            return false;
        }
        e.displayAddress = displayAddress;
        if (walletProvider != null && !walletProvider.isBlank()) {
            e.walletProvider = walletProvider;
        }
        e.verifiedAtUtc = Instant.now();
        jpa.save(e);
        return true;
    }

    static WalletIdentity toDomain(WalletIdentityEntity e) {
        WalletIdentity identity = new WalletIdentity();
        identity.setId(e.id);
        identity.setUserId(e.userId);
        identity.setFamily(e.family);
        identity.setNormalizedAddress(e.normalizedAddress);
        identity.setDisplayAddress(e.displayAddress);
        identity.setWalletProvider(e.walletProvider);
        identity.setVerifiedAtUtc(e.verifiedAtUtc);
        return identity;
    }
}
