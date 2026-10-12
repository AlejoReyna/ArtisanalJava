package dev.alexisreyna.artisanalbrew.infrastructure.security;

import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.ApplicationUserEntity;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.RoleEntity;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.ApplicationUserJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.RoleJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.UserRoleJpaRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Loads account users by user name or e-mail (mirrors
 * {@code SignInManager.PasswordSignInAsync(email, …)}). Lockout state comes
 * from the mapped {@code AspNetUsers} columns.
 */
@Service
@Transactional(readOnly = true)
public class ApplicationUserDetailsService implements UserDetailsService {
    private final ApplicationUserJpaRepository users;
    private final UserRoleJpaRepository userRoles;
    private final RoleJpaRepository roles;

    public ApplicationUserDetailsService(ApplicationUserJpaRepository users, UserRoleJpaRepository userRoles,
                                         RoleJpaRepository roles) {
        this.users = users;
        this.userRoles = userRoles;
        this.roles = roles;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalized = username == null ? "" : username.toUpperCase(java.util.Locale.ROOT);
        ApplicationUserEntity user = users.findByNormalizedUserName(normalized)
                .or(() -> users.findByNormalizedEmail(normalized))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        List<SimpleGrantedAuthority> authorities = userRoles.findByUserId(user.id).stream()
                .map(link -> roles.findById(link.roleId).map(role -> toAuthority(role)).orElse(null))
                .filter(a -> a != null)
                .toList();
        boolean unlocked = user.lockoutEnd == null || !user.lockoutEnd.isAfter(Instant.now());
        return User.builder()
                .username(user.id)
                .password(user.passwordHash == null ? "" : user.passwordHash)
                .authorities(authorities)
                .accountLocked(!unlocked)
                .build();
    }

    static SimpleGrantedAuthority toAuthority(RoleEntity role) {
        String name = role.normalizedName == null || role.normalizedName.isBlank()
                ? (role.name == null ? "" : role.name.toUpperCase(java.util.Locale.ROOT))
                : role.normalizedName;
        return new SimpleGrantedAuthority("ROLE_" + name);
    }
}
