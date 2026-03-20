package com.spring.security.authentication.handler.auth;

import com.spring.security.authentication.handler.auth.jwt.constant.JWTConstants;
import com.spring.security.authentication.handler.auth.jwt.dto.JwtTokenUserLoginInfo;
import com.spring.security.authentication.handler.auth.jwt.service.JwtService;
import com.spring.security.web.constant.RedisCache;
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
import org.redisson.codec.TypedJsonJackson3Codec;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
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
    private final JsonMapper jsonMapper;

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
        UserLoginInfo currentUser = Optional.of(authentication)
                .map(Authentication::getPrincipal)
                .filter(UserLoginInfo.class::isInstance)
                .map(UserLoginInfo.class::cast)
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("当前未找到有效的登录用户信息"));
        JwtTokenUserLoginInfo jwtTokenUserLoginInfo =
                new JwtTokenUserLoginInfo(currentUser.getSessionId(), currentUser.getUsername());
        // 一些特殊的登录参数。比如三方登录，需要额外返回一个字段是否需要跳转的绑定已有账号页面
        Map<String, Object> additionalInfo = Optional.ofNullable(authentication.getDetails())
                .map(details -> jsonMapper.convertValue(details, new TypeReference<Map<String, Object>>() {}))
                .orElse(Map.of());
        boolean hasAccount =
                authentication.getDetails() == null || Boolean.FALSE.equals(additionalInfo.get("isNewUser"));
        // 生成token和refreshToken
        String token = hasAccount
                ? jwtService.generateTokenFromUsername(
                        currentUser.getUsername(), jwtTokenUserLoginInfo, JWTConstants.TOKEN_EXPIRED_TIME)
                : null;
        String refreshToken = hasAccount
                ? jwtService.generateTokenFromUsername(
                        currentUser.getUsername(), jwtTokenUserLoginInfo, JWTConstants.REFRESH_TOKEN_EXPIRED_TIME)
                : null;
        if (hasAccount)
            redissonClient
                    .getBucket(
                            RedisCache.USER_INFO.formatted(jwtTokenUserLoginInfo.username()),
                            new TypedJsonJackson3Codec(UserLoginInfo.class))
                    .set(currentUser);

        LoginResponse loginResponse = new LoginResponse(token, refreshToken, additionalInfo);
        // UTF_8编码 APPLICATION_JSON_VALUE防止出现乱码
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.OK.value());
        jsonMapper.writeValue(response.getOutputStream(), loginResponse);
    }
}
