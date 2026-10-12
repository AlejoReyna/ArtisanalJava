package dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo;

import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleJpaRepository extends JpaRepository<UserRoleEntity, UserRoleEntity.Key> {
    List<UserRoleEntity> findByUserId(String userId);

    boolean existsByUserIdAndRoleId(String userId, String roleId);

    void deleteByUserId(String userId);
}
