package com.spring.security.domain.model.entity;

import com.spring.security.domain.model.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@Entity
@Table(
        name = "sys_role_permission_rel",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_role_permission",
                    columnNames = {"role_id", "permission_id"})
        },
        comment = "角色权限关联表")
public class RolePermission extends BaseEntity {

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            comment = "角色Id",
            name = "role_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_role_permission_role_id"))
    private Role role;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            comment = "权限Id",
            name = "permission_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_role_permission_permission_id"))
    private Permission permission;
}
