package com.spring.security.domain.model.entity;

import com.spring.security.domain.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Entity
@Table(
        name = "sys_role",
        uniqueConstraints = {@UniqueConstraint(name = "uk_role_code", columnNames = "code")},
        comment = "角色表")
@Schema(title = "系统角色实体")
public class Role extends BaseEntity {

    @Schema(title = "角色编码")
    @Column(comment = "角色编码", name = "code", length = 10, nullable = false)
    private String code;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<UserRole> users = new HashSet<>();

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<RolePermission> rolePermissions = new HashSet<>();
}
