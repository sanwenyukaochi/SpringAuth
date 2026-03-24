package com.spring.security.domain.repository;

import com.spring.security.domain.model.entity.User;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<@NonNull User, @NonNull Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    @EntityGraph(
            attributePaths = {
                "userRoles",
                "userRoles.role",
                "userRoles.role.rolePermissions",
                "userRoles.role.rolePermissions.permission"
            })
    Optional<User> findWithAuthoritiesByUsername(String username);

    @EntityGraph(
            attributePaths = {
                "userRoles",
                "userRoles.role",
                "userRoles.role.rolePermissions",
                "userRoles.role.rolePermissions.permission"
            })
    Optional<User> findWithAuthoritiesByEmail(String email);

    @EntityGraph(
            attributePaths = {
                "userRoles",
                "userRoles.role",
                "userRoles.role.rolePermissions",
                "userRoles.role.rolePermissions.permission"
            })
    Optional<User> findWithAuthoritiesByPhone(String phone);

    boolean existsByUsername(String user);

    boolean existsByEmail(String email);

    Slice<User> findByOrderByUsernameAsc(Pageable pageable);

    Window<User> findFirst6ByOrderByUsernameAsc(KeysetScrollPosition position);
}
