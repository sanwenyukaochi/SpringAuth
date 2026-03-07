package com.spring.security.domain.model.entity;

import com.spring.security.domain.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Table(
        name = "sys_user_identity",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_user_identity_user_provider",
                    columnNames = {"user_id", "provider"})
        },
        comment = "用户第三方登录绑定表")
@Schema(name = "UserIdentity", title = "用户登录绑定对象", description = "存储用户绑定的第三方登录信息")
public class UserIdentity extends BaseEntity {

    @Schema(title = "所属用户", description = "该第三方登录方式属于哪个用户")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            comment = "用户ID",
            name = "user_id",
            referencedColumnName = "id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_identity_user"))
    @ToString.Exclude
    private User user;

    @Schema(title = "登录提供商", example = "GITHUB", description = "第三方登录来源，如 GITHUB、GOOGLE、WECHAT")
    @Column(comment = "登录提供商", name = "provider", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    @Schema(title = "第三方用户唯一ID", example = "123456", description = "第三方平台返回的用户唯一ID，用于绑定")
    @Column(comment = "第三方用户唯一ID", name = "provider_user_id", nullable = false, length = 64)
    private Long providerUserId;

    public enum AuthProvider {
        EMAIL,
        GITHUB,
        GOOGLE,
        WECHAT
    }
}
