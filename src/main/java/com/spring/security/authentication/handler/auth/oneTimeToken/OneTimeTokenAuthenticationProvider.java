package com.spring.security.authentication.handler.auth.oneTimeToken;

import com.spring.security.authentication.handler.auth.UserLoginInfo;
import com.spring.security.authentication.handler.auth.oneTimeToken.service.RedisOneTimeTokenService;
import com.spring.security.authentication.handler.authorization.Authority;
import com.spring.security.web.service.UserService;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Slf4j
@Component
@RequiredArgsConstructor
public class OneTimeTokenAuthenticationProvider implements AuthenticationProvider {
    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
    private final RedisOneTimeTokenService redisOneTimeTokenService;
    private final UserService userService;
    private static final String AUTHORITY = Authority.OTT_AUTHORITY;

    @Override
    public Authentication authenticate(@NonNull Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(
                OneTimeTokenAuthenticationToken.class,
                authentication,
                () -> this.messages.getMessage("OneTimeTokenAuthenticationToken.onlySupports", "仅支持一次性身份验证提供程序"));
        OneTimeTokenAuthenticationToken otpAuthenticationToken = (OneTimeTokenAuthenticationToken) authentication;
        // 获取用户提交的用户名
        String token = otpAuthenticationToken.getToken() == null ? "NONE_PROVIDED" : otpAuthenticationToken.getToken();
        // 验证用户信息
        OneTimeToken oneTimeToken = this.redisOneTimeTokenService.consume(otpAuthenticationToken);
        Optional.ofNullable(oneTimeToken)
                .orElseThrow(() -> new BadCredentialsException(
                        this.messages.getMessage("jwtTokenAuthenticationProvider.sessionExpired", "错误的凭证")));
        // 查询用户信息
        UserLoginInfo userLoginInfo = userService.loadUserByUsername(oneTimeToken.getUsername());
        userLoginInfo.getAuthorities().add(FactorGrantedAuthority.fromAuthority(AUTHORITY));
        log.debug("用户信息查询成功，用户: {}", userLoginInfo.getUsername());
        // 构造成功结果
        // 认证通过，使用 Authenticated 为 true 的构造函数
        OneTimeTokenAuthenticationToken result =
                OneTimeTokenAuthenticationToken.authenticated(userLoginInfo, userLoginInfo.getAuthorities());
        // 必须转化成Map
        result.setDetails(authentication.getDetails());
        log.debug("用户名认证成功，用户: {}", userLoginInfo.getUsername());
        return result;
    }

    @Override
    public boolean supports(@NonNull Class<?> authentication) {
        return OneTimeTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
