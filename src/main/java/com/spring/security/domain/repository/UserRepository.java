package com.spring.security.domain.repository;

import com.spring.security.domain.model.entity.User;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<@NonNull User, @NonNull Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String user);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);
}
