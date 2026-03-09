package com.spring.security.authentication.handler.auth;

import com.spring.security.authentication.handler.auth.jwt.constant.JWTConstants;
import com.spring.security.authentication.handler.auth.jwt.dto.JwtTokenUserLoginInfo;
import com.spring.security.authentication.handler.auth.jwt.service.JwtService;
import com.spring.security.common.cache.constant.RedisCache;
import com.spring.security.common.web.enums.BaseCode;
import com.spring.security.common.web.exception.BaseException;
import com.spring.security.domain.model.dto.Result;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.redisson.api.RedissonClient;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * 认证成功/登录成功 事件处理器
 */
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler extends AbstractAuthenticationTargetUrlRequestHandler
        implements AuthenticationSuccessHandler {
    private final JwtService jwtService;
    private final RedissonClient redissonClient;

    @PostConstruct
    public void disableRedirectStrategy() {
        setRedirectStrategy((_, _, _) -> {
            // 更改重定向策略，前后端分离项目，后端使用RestFul风格，无需做重定向
            // Do nothing, no redirects in REST
        });
    }

    @Override
    public void onAuthenticationSuccess(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Authentication authentication)
            throws IOException {
        @SuppressWarnings("unchecked")
        Map<String, Object> additionalInfo = Optional.ofNullable(authentication.getDetails())
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .orElse(Map.of());

        UserLoginInfo currentUser = Optional.of(authentication)
                .map(Authentication::getPrincipal)
                .filter(UserLoginInfo.class::isInstance)
                .map(UserLoginInfo.class::cast)
                .orElseThrow(() -> new BaseException(BaseCode.AUTHENTICATION_TYPE_ERROR));
        boolean isNewUser = Boolean.TRUE.equals(additionalInfo.get("isNewUser"));
        String token = null;
        String refreshToken = null;
        if (!isNewUser) {
            JwtTokenUserLoginInfo jwtTokenUserLoginInfo =
                    new JwtTokenUserLoginInfo(currentUser.getSessionId(), currentUser.getUsername());
            // 生成token和refreshToken
            token = jwtService.generateTokenFromUsername(
                    currentUser.getUsername(), jwtTokenUserLoginInfo, JWTConstants.TOKEN_EXPIRED_TIME);
            refreshToken = jwtService.generateTokenFromUsername(
                    currentUser.getUsername(), jwtTokenUserLoginInfo, JWTConstants.REFRESH_TOKEN_EXPIRED_TIME);
            redissonClient
                    .getBucket(
                            RedisCache.USER_INFO.formatted(jwtTokenUserLoginInfo.username()),
                            new TypedJsonJacksonCodec(UserLoginInfo.class))
                    .set(currentUser);
        }

        LoginResponse loginResponse = new LoginResponse(token, refreshToken, additionalInfo);
        // UTF_8编码 APPLICATION_JSON_VALUE防止出现乱码
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.OK.value());
        JsonMapper.shared().writeValue(response.getOutputStream(), Result.success(loginResponse));
    }
}
