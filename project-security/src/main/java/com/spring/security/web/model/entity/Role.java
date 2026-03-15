package com.spring.security.web.model.entity;

import com.spring.security.web.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
    @Column(comment = "权限标识", name = "code", length = 10, nullable = false)
    private String code;
}
