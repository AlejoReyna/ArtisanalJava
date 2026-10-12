package dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo;

import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserProfileJpaRepository extends JpaRepository<UserProfileEntity, UUID> {
}
