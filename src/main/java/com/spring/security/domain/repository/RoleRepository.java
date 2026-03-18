package com.spring.security.domain.repository;

import com.spring.security.domain.model.entity.Role;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<@NonNull Role, @NonNull Long> {}
