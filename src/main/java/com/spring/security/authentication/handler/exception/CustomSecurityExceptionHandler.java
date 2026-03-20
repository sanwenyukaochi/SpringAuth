package com.spring.security.authentication.handler.exception;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;

/**
 * 捕捉Spring security filter chain 中抛出的未知异常
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomSecurityExceptionHandler extends OncePerRequestFilter {

    private final JsonMapper jsonMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (AuthenticationException e) {
            log.warn("认证异常：msg={}", e.getMessage(), e);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            jsonMapper.writeValue(
                    response.getOutputStream(),
                    ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage()));
        } catch (AccessDeniedException e) {
            log.warn("鉴权异常：msg={}", e.getMessage(), e);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            jsonMapper.writeValue(
                    response.getOutputStream(), ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage()));
        } catch (Exception e) {
            log.warn("未知异常：msg={}", e.getMessage(), e);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            jsonMapper.writeValue(
                    response.getOutputStream(),
                    ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
    }
}
