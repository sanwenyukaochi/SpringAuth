package org.oauth.client.authentication.github.dto;

public record GitHubOAuth2Meta(
        Long providerUserId,
        String login,
        String name,
        String email) {}
