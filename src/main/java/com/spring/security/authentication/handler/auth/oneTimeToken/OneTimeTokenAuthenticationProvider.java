package com.spring.security.authentication.handler.auth.oneTimeToken;

import com.spring.security.authentication.handler.auth.UserLoginInfo;
import com.spring.security.authentication.handler.auth.oneTimeToken.service.RedisOneTimeTokenService;
import com.spring.security.common.web.enums.BaseCode;
import com.spring.security.common.web.exception.BaseException;
import com.spring.security.domain.model.entity.User;
import com.spring.security.domain.repository.UserRepository;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Slf4j
@Component
@RequiredArgsConstructor
public class OneTimeTokenAuthenticationProvider implements AuthenticationProvider {
    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
    private static final String AUTHORITY = FactorGrantedAuthority.OTT_AUTHORITY;
    private final UserRepository userRepository;
    private final RedisOneTimeTokenService redisOneTimeTokenService;

    @Override
    public Authentication authenticate(@NonNull Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(
                OneTimeTokenAuthenticationToken.class,
                authentication,
                () -> this.messages.getMessage("OneTimeTokenAuthenticationToken.onlySupports", "仅支持一次性身份验证提供程序"));
        OneTimeTokenAuthenticationToken otpAuthenticationToken = (OneTimeTokenAuthenticationToken) authentication;
        // 获取用户提交的用户名
        String token = otpAuthenticationToken.getToken() == null ? "NONE_PROVIDED" : otpAuthenticationToken.getToken();
        // 查询用户信息
        OneTimeToken oneTimeToken = retrieveUser(token, (OneTimeTokenAuthenticationToken) authentication);
        // 验证用户信息
        UserLoginInfo userLoginInfo = additionalAuthenticationChecks(
                oneTimeToken.getUsername(), (OneTimeTokenAuthenticationToken) authentication);
        // 构造成功结果
        return createSuccessAuthentication(otpAuthenticationToken, userLoginInfo);
    }

    @Override
    public boolean supports(@NonNull Class<?> authentication) {
        return OneTimeTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }

    protected Authentication createSuccessAuthentication(Authentication authentication, UserLoginInfo userLoginInfo) {
        // 认证通过，使用 Authenticated 为 true 的构造函数
        Collection<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(FactorGrantedAuthority.fromAuthority(AUTHORITY));
        OneTimeTokenAuthenticationToken result =
                OneTimeTokenAuthenticationToken.authenticated(userLoginInfo, authorities);
        // 必须转化成Map
        result.setDetails(authentication.getDetails());
        log.debug("用户名认证成功，用户: {}", userLoginInfo.getUsername());
        return result;
    }

    protected UserLoginInfo additionalAuthenticationChecks(
            String username, OneTimeTokenAuthenticationToken authentication) throws AuthenticationException {
        User loadedUser =
                userRepository.findByUsername(username).orElseThrow(() -> new BaseException(BaseCode.USER_NOT_FOUND));
        log.debug("用户信息查询成功，用户: {}", loadedUser.getUsername());
        return new UserLoginInfo(
                UUID.randomUUID().toString(),
                loadedUser.getId(),
                loadedUser.getUsername(),
                loadedUser.getPassword(),
                loadedUser.getPhone(),
                loadedUser.getEmail(),
                loadedUser.getAccountNonLocked(),
                loadedUser.getAccountNonLocked(),
                loadedUser.getCredentialsNonExpired(),
                loadedUser.getEnabled(),
                loadedUser.getMfaSecret(),
                loadedUser.getMfaEnabled());
    }

    protected OneTimeToken retrieveUser(String token, OneTimeTokenAuthenticationToken authentication)
            throws AuthenticationException {
        OneTimeToken consumed = this.redisOneTimeTokenService.consume(authentication);
        Optional.ofNullable(consumed)
                .orElseThrow(() -> new BadCredentialsException(
                        this.messages.getMessage("jwtTokenAuthenticationProvider.sessionExpired", "错误的凭证")));
        return consumed;
    }
}
