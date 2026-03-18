package com.spring.security.domain.repository;

import com.spring.security.domain.model.entity.User;
import com.spring.security.domain.model.entity.UserRole;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends JpaRepository<@NonNull UserRole, @NonNull Long> {
    @EntityGraph(attributePaths = {"role"})
    List<UserRole> findByUser(User user);
}
