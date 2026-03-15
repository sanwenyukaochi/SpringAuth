package com.spring.security.authentication.handler.exception;

import com.spring.security.common.web.enums.BaseCode;
import com.spring.security.domain.model.dto.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * 认证失败时，会执行这个方法。将失败原因告知客户端
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationExceptionHandler implements AuthenticationEntryPoint {
    private final JsonMapper jsonMapper;

    @Override
    public void commence(
            @NonNull HttpServletRequest request,
            HttpServletResponse response,
            @NonNull AuthenticationException authenticationException)
            throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        log.warn("登录异常：msg={}", authenticationException.getMessage(), authenticationException);
        jsonMapper.writeValue(response.getOutputStream(), Result.error(BaseCode.AUTHENTICATION_ERROR, null));
    }
}
