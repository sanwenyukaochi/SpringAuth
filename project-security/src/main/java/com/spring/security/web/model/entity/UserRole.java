package com.spring.security.web.model.entity;

import com.spring.security.web.model.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "sys_user_role_rel",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_user_role",
                    columnNames = {"user_id", "role_id"})
        },
        comment = "用户角色关联表")
public class UserRole extends BaseEntity {

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            comment = "用户Id",
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_role_user_id"))
    private User user;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            comment = "角色Id",
            name = "role_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_role_role_id"))
    private Role role;
}
