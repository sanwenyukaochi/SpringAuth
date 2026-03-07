package com.spring.security.domain.repository;

import com.spring.security.domain.model.entity.UserIdentity;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserIdentityRepository extends JpaRepository<@NonNull UserIdentity, @NonNull Long> {

    List<UserIdentity> findByProviderUserId(Long providerUserId);

    Optional<UserIdentity> findByProviderUserIdAndProvider(Long id, UserIdentity.AuthProvider authProvider);
}
