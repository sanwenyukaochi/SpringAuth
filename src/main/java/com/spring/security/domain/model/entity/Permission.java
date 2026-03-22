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
        name = "sys_permission",
        uniqueConstraints = {@UniqueConstraint(name = "uk_permission_code", columnNames = "code")},
        comment = "权限表")
@Schema(title = "系统权限实体")
public class Permission extends BaseEntity {

    @Schema(title = "权限编码")
    @Column(comment = "权限编码", name = "code", length = 30, nullable = false)
    private String code;

    @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<RolePermission> roles = new HashSet<>();
}
