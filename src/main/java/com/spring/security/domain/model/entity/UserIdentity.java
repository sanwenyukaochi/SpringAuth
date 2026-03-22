package com.spring.security.domain.model.entity;

import com.spring.security.domain.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter
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
@Schema(title = "用户登录绑定对象")
public class UserIdentity extends BaseEntity {

    @Schema(title = "用户名")
    @Column(comment = "用户名", name = "user_id", nullable = false, length = 20)
    private Long userId;

    @Schema(title = "登录提供商")
    @Column(comment = "登录提供商", name = "provider", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Schema(title = "第三方用户唯一ID")
    @Column(comment = "第三方用户唯一ID", name = "provider_user_id", nullable = false, length = 64)
    private Long providerUserId;

    @RequiredArgsConstructor
    public enum Provider {
        GITHUB("GitHub"),
        GOOGLE("Google"),
        WECHAT("WeChat"),
        ;
        private final String desc;
    }
}
