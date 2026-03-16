package com.spring.security.web.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "GitHub OAuth 登录请求")
public record GitHubOAuthLoginRequest(
        @Schema(title = "GitHub 用户 ID") Long providerUserId,
        @Schema(title = "GitHub 登录名") String login,
        @Schema(title = "GitHub 昵称") String name,
        @Schema(title = "GitHub 邮箱") String email) {}
