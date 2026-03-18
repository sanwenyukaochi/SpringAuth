package com.spring.security.authentication.handler.auth.jwt;

import com.spring.security.authentication.handler.auth.UserLoginInfo;
import com.spring.security.authentication.handler.auth.jwt.dto.JwtTokenUserLoginInfo;
import com.spring.security.authentication.handler.auth.jwt.service.JwtService;
import com.spring.security.web.constant.RedisCache;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.redisson.api.RedissonClient;
import org.redisson.codec.TypedJsonJackson3Codec;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * JWT认证提供者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenAuthenticationProvider implements AuthenticationProvider {
    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
    private final JwtService jwtService;
    private final RedissonClient redissonClient;

    @Override
    @SneakyThrows
    public Authentication authenticate(@NonNull Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(
                JwtTokenAuthenticationToken.class,
                authentication,
                () -> this.messages.getMessage("JwtTokenAuthenticationProvider.onlySupports", "仅支持JWT身份验证提供程序"));
        JwtTokenAuthenticationToken jwtTokenAuthenticationToken = (JwtTokenAuthenticationToken) authentication;
        // 获取用户提交的JWT
        String jwtToken = (jwtTokenAuthenticationToken.getJwtToken() == null
                ? "NONE_PROVIDED"
                : jwtTokenAuthenticationToken.getJwtToken());
        // 查询用户信息
        JwtTokenUserLoginInfo jwtTokenUserLoginInfo = jwtService.validateJwtToken(jwtToken);
        Optional.ofNullable(jwtTokenUserLoginInfo)
                .orElseThrow(() -> new BadCredentialsException(
                        this.messages.getMessage("jwtTokenAuthenticationProvider.sessionExpired", "错误的凭证")));
        // 验证用户信息
        UserLoginInfo userLoginInfo = (UserLoginInfo) redissonClient
                .getBucket(
                        RedisCache.USER_INFO.formatted(jwtTokenUserLoginInfo.username()),
                        new TypedJsonJackson3Codec(UserLoginInfo.class))
                .get();
        log.debug("用户信息解析成功，用户: {}", userLoginInfo.getUsername());
        // 构造成功结果
        // 认证通过，使用 Authenticated 为 true 的构造函数
        JwtTokenAuthenticationToken result = JwtTokenAuthenticationToken.authenticated(userLoginInfo, List.of());
        // 必须转化成Map
        result.setDetails(authentication.getDetails());
        log.debug("JWT认证成功，用户: {}", userLoginInfo.getUsername());
        return result;
    }

    @Override
    public boolean supports(@NonNull Class<?> authentication) {
        return JwtTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
