package com.spring.security.authentication.handler.auth.oneTimeToken.dto;

public record RedisOneTimeToken(String tokenValue, String username, Long expiresAt) {}
