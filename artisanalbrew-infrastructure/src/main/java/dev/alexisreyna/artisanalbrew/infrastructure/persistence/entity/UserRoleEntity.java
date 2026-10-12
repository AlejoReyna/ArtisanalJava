package dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;

/** JPA row for {@code AspNetUserRoles} (composite key). */
@Entity
@Table(name = "\"AspNetUserRoles\"")
@IdClass(UserRoleEntity.Key.class)
public class UserRoleEntity {
    @Id
    @Column(name = "\"UserId\"")
    public String userId;

    @Id
    @Column(name = "\"RoleId\"")
    public String roleId;

    public static final class Key implements Serializable {
        public String userId;
        public String roleId;

        public Key() {
        }

        public Key(String userId, String roleId) {
            this.userId = userId;
            this.roleId = roleId;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Key other)) {
                return false;
            }
            return Objects.equals(userId, other.userId) && Objects.equals(roleId, other.roleId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, roleId);
        }
    }
}
