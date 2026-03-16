package org.oauth.client.dto;

public record GitHubOAuthLoginRequest(Long providerUserId, String login, String name, String email) {}
