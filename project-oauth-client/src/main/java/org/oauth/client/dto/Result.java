package org.oauth.client.dto;

public record Result<T>(String code, String message, T data) {}
