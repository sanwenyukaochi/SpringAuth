package com.spring.security.authentication.handler.auth.github.dto;

import com.spring.security.domain.model.entity.UserIdentity;

public record GitHubOAuth2Meta(
        UserIdentity.Provider provider,
        Long providerUserId,
        String login,
        String name,
        String email,
        Boolean isNewUser) {}
