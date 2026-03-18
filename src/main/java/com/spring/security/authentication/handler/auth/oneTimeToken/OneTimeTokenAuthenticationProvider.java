package com.spring.security.authentication.handler.auth.oneTimeToken;

import com.spring.security.authentication.handler.auth.UserLoginInfo;
import com.spring.security.authentication.handler.auth.oneTimeToken.service.RedisOneTimeTokenService;
import com.spring.security.authentication.handler.authorization.Authority;
import com.spring.security.domain.model.entity.Role;
import com.spring.security.domain.model.entity.User;
import com.spring.security.domain.model.entity.UserRole;
import com.spring.security.domain.repository.UserRepository;
import com.spring.security.domain.repository.UserRoleRepository;
import com.spring.security.web.enums.BaseCode;
import com.spring.security.web.exception.BaseException;
import java.util.*;
import java.util.stream.Collectors;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Slf4j
@Component
@RequiredArgsConstructor
public class OneTimeTokenAuthenticationProvider implements AuthenticationProvider {
    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
    private final UserRepository userRepository;
    private final RedisOneTimeTokenService redisOneTimeTokenService;
    private final UserRoleRepository userRoleRepository;
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
        User loadedUser = userRepository
                .findByUsername(oneTimeToken.getUsername())
                .orElseThrow(() -> new BaseException(BaseCode.USER_NOT_FOUND));
        Collection<GrantedAuthority> authorities = userRoleRepository.findByUser(loadedUser).stream()
                .map(UserRole::getRole)
                .map(Role::getCode)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        authorities.add(FactorGrantedAuthority.fromAuthority(AUTHORITY));
        log.debug("用户信息查询成功，用户: {}", loadedUser.getUsername());
        UserLoginInfo userLoginInfo = new UserLoginInfo(
                UUID.randomUUID().toString(),
                loadedUser.getId(),
                loadedUser.getUsername(),
                loadedUser.getPassword(),
                loadedUser.getPhone(),
                loadedUser.getEmail(),
                loadedUser.getAccountNonLocked(),
                loadedUser.getAccountNonExpired(),
                loadedUser.getCredentialsNonExpired(),
                loadedUser.getEnabled(),
                loadedUser.getMfaSecret(),
                loadedUser.getMfaEnabled(),
                authorities);
        // 构造成功结果
        // 认证通过，使用 Authenticated 为 true 的构造函数
        OneTimeTokenAuthenticationToken result =
                OneTimeTokenAuthenticationToken.authenticated(userLoginInfo, authorities);
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
