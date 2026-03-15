package com.spring.security.web.model.entity;

import com.spring.security.web.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Entity
@Table(
        name = "sys_user",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_user_username", columnNames = "username"),
            @UniqueConstraint(name = "uk_user_email", columnNames = "email")
        },
        comment = "用户表")
@Schema(title = "系统用户实体")
public class User extends BaseEntity {

    @Schema(title = "用户名")
    @Column(comment = "用户名", name = "username", nullable = false, length = 20)
    private String username;

    @Schema(title = "密码", accessMode = Schema.AccessMode.WRITE_ONLY)
    @Column(comment = "用户密码", name = "password_hash", nullable = false, length = 120)
    private String password;

    @Schema(title = "邮箱")
    @Column(comment = "邮箱", name = "email", nullable = false, length = 50)
    private String email;

    @Schema(title = "手机号")
    @Column(comment = "手机号", name = "phone", nullable = false, length = 50)
    private String phone;

    @Schema(title = "账户是否锁定")
    @Column(comment = "账户是否未锁定（true=正常，false=锁定）", name = "account_non_locked", nullable = false)
    private Boolean accountNonLocked = true;

    @Schema(title = "账户是否过期")
    @Column(comment = "账户是否未过期（true=有效，false=过期）", name = "account_non_expired", nullable = false)
    private Boolean accountNonExpired = true;

    @Schema(title = "密码是否过期")
    @Column(comment = "密码是否未过期（true=有效，false=已过期）", name = "credentials_non_expired", nullable = false)
    private Boolean credentialsNonExpired = true;

    @Schema(title = "是否启用")
    @Column(comment = "状态（true=启用，false=禁用）", name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Schema(title = "双因素认证密钥（TOTP Secret，用于 Google Authenticator 等）")
    @Column(comment = "双因素认证密钥", name = "mfa_secret", length = 64)
    private String mfaSecret;

    @Schema(title = "是否启用双因素认证")
    @Column(comment = "是否启用双因素认证（true=启用，false=未启用）", name = "mfa_enabled", nullable = false)
    private Boolean mfaEnabled = false;
}
