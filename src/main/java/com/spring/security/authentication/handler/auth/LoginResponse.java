package com.spring.security.authentication.handler.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record LoginResponse(String token, String refreshToken, Map<String, Object> additionalInfo) {}
