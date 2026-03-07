package com.spring.security.authentication.handler.exception;

import com.spring.security.common.web.enums.BaseCode;
import com.spring.security.common.web.exception.BaseException;
import com.spring.security.domain.model.dto.Result;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    @Override
    public void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (BaseException e) {
            log.warn("认证异常：code={}, msg={}, status={}", e.getCode(), e.getMessage(), e.getHttpStatus(), e);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(e.getHttpStatus().value());
            JsonMapper.shared().writeValue(response.getOutputStream(), Result.error(e.getCode(), e.getMessage(), null));
        } catch (AuthenticationException | AccessDeniedException e) {
            log.warn("Spring Security异常：msg={}", e.getMessage(), e);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            JsonMapper.shared()
                    .writeValue(
                            response.getOutputStream(),
                            Result.error(BaseCode.SYSTEM_ERROR.getCode(), e.getMessage(), null));
        } catch (Exception e) {
            log.warn("未知异常：msg={}", e.getMessage(), e);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            JsonMapper.shared().writeValue(response.getOutputStream(), Result.error(BaseCode.SYSTEM_ERROR, null));
        }
    }
}
