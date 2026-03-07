package com.spring.security.domain.model.entity;

import com.spring.security.domain.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.persistence.Column;
import java.util.ArrayList;
import java.util.List;
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
@Schema(name = "User", title = "用户对象", description = "系统用户实体，包含认证和状态信息")
public class User extends BaseEntity {

    @Schema(title = "用户名", example = "alice", description = "系统登录名，唯一标识用户")
    @Column(comment = "用户名", name = "username", nullable = false, length = 20)
    private String username;

    @Schema(title = "密码", description = "加密后的密码，不会在接口返回中显示", accessMode = Schema.AccessMode.WRITE_ONLY)
    @Column(comment = "用户密码", name = "password", nullable = false, length = 120)
    private String password;

    @Schema(title = "邮箱", example = "alice@example.com", description = "用户绑定邮箱，用于找回密码或登录")
    @Column(comment = "邮箱", name = "email", nullable = false, length = 50)
    private String email;

    @Schema(title = "手机号", example = "12345678910", description = "用户绑定手机号，用于找回密码或登录")
    @Column(comment = "手机号", name = "phone", nullable = false, length = 50)
    private String phone;

    @Schema(title = "账户是否未锁定", description = "true 表示正常，false 表示锁定")
    @Column(comment = "账户是否未锁定（true=正常，false=锁定）", name = "account_non_locked", nullable = false)
    private Boolean accountNonLocked = true;

    @Schema(title = "账户是否未过期", description = "true 表示有效，false 表示过期")
    @Column(comment = "账户是否未过期（true=有效，false=过期）", name = "account_non_expired", nullable = false)
    private Boolean accountNonExpired = true;

    @Schema(title = "密码是否未过期", description = "true 表示有效，false 表示密码已过期")
    @Column(comment = "密码是否未过期（true=有效，false=已过期）", name = "credentials_non_expired", nullable = false)
    private Boolean credentialsNonExpired = true;

    @Schema(title = "是否启用", description = "true 表示账户启用，false 表示禁用")
    @Column(comment = "状态（true=启用，false=禁用）", name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Schema(title = "双因素认证密钥", example = "JBOSSWS3DPKG3PXP", description = "TOTP Secret，用于 Google Authenticator 等")
    @Column(comment = "双因素认证密钥（TOTP Secret，用于 Google Authenticator 等）", name = "two_factor_secret", length = 64)
    private String twoFactorSecret;

    @Schema(title = "是否启用双因素认证", description = "true 表示启用，false 表示未启用")
    @Column(comment = "是否启用双因素认证（true=启用，false=未启用）", name = "two_factor_enabled", nullable = false)
    private Boolean twoFactorEnabled = false;

    @Schema(title = "用户绑定的所有第三方登录信息", description = "一个用户可以绑定多个第三方账号（GitHub、Google、微信等）")
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<UserIdentity> identities = new ArrayList<>();
}
