package com.spring.security.authentication.handler.auth.jwt;

import com.spring.security.authentication.handler.auth.UserLoginInfo;
import com.spring.security.authentication.handler.auth.jwt.dto.JwtTokenUserLoginInfo;
import com.spring.security.authentication.handler.auth.jwt.service.JwtService;
import com.spring.security.common.cache.constant.RedisCache;
import com.spring.security.common.web.enums.BaseCode;
import com.spring.security.common.web.exception.BaseException;
import com.spring.security.domain.model.entity.User;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.redisson.api.RedissonClient;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import tools.jackson.databind.json.JsonMapper;

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
        User user = retrieveUser(jwtToken, jwtTokenAuthenticationToken);
        // 验证用户信息
        additionalAuthenticationChecks(user, (JwtTokenAuthenticationToken) authentication);
        // 构造成功结果
        return createSuccessAuthentication(jwtTokenAuthenticationToken, user);
    }

    @Override
    public boolean supports(@NonNull Class<?> authentication) {
        return JwtTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }

    protected Authentication createSuccessAuthentication(Authentication authentication, User user) {
        UserLoginInfo userLoginInfo = (UserLoginInfo) redissonClient
                .getBucket(
                        RedisCache.USER_INFO.formatted(user.getUsername()),
                        new TypedJsonJacksonCodec(UserLoginInfo.class))
                .get();
        // 认证通过，使用 Authenticated 为 true 的构造函数
        JwtTokenAuthenticationToken result = new JwtTokenAuthenticationToken(userLoginInfo, List.of());
        // 必须转化成Map
        result.setDetails(JsonMapper.shared().convertValue(authentication.getDetails(), Map.class));
        log.debug("JWT认证成功，用户: {}", userLoginInfo.getUsername());
        return result;
    }

    protected User retrieveUser(String jwtToken, JwtTokenAuthenticationToken authentication)
            throws AuthenticationException {
        JwtTokenUserLoginInfo jwtTokenUserLoginInfo = jwtService.validateJwtToken(jwtToken);
        User loadedUser = new User();
        loadedUser.setUsername(jwtTokenUserLoginInfo.username());
        authentication.setDetails(null);
        log.debug("用户信息解析成功，用户: {}", jwtTokenUserLoginInfo.username());
        return loadedUser;
    }

    protected void additionalAuthenticationChecks(User user, JwtTokenAuthenticationToken authentication)
            throws AuthenticationException {
        String presentedJwtToken = authentication.getJwtToken();
        UserLoginInfo userLoginInfo = (UserLoginInfo) redissonClient
                .getBucket(
                        RedisCache.USER_INFO.formatted(user.getUsername()),
                        new TypedJsonJacksonCodec(UserLoginInfo.class))
                .get();
        Optional.ofNullable(presentedJwtToken)
                .orElseThrow(() -> new BadCredentialsException(
                        this.messages.getMessage("jwtTokenAuthenticationProvider.sessionExpired", "错误的凭证")));
        Optional.ofNullable(userLoginInfo).orElseThrow(() -> new BaseException(BaseCode.TOKEN_NOT_FIND_IN_REDIS));
    }
}
