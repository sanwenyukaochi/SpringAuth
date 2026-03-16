package com.spring.security.authentication.handler.auth.user;

import com.spring.security.authentication.handler.auth.UserLoginInfo;
import com.spring.security.authentication.handler.authorization.Authority;
import com.spring.security.domain.model.entity.User;
import com.spring.security.domain.repository.UserRepository;
import com.spring.security.web.enums.BaseCode;
import com.spring.security.web.exception.BaseException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 帐号密码登录认证
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UsernameAuthenticationProvider implements AuthenticationProvider {
    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final String AUTHORITY = Authority.PASSWORD_AUTHORITY;

    @Override
    public Authentication authenticate(@NonNull Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(
                UsernameAuthenticationToken.class,
                authentication,
                () -> this.messages.getMessage("UsernameAuthenticationProvider.onlySupports", "仅支持用户名身份验证提供程序"));
        UsernameAuthenticationToken usernameAuthenticationToken = (UsernameAuthenticationToken) authentication;
        // 获取用户提交的用户名
        String username = usernameAuthenticationToken.getUsername();
        // 查询用户信息
        UserLoginInfo userLoginInfo = retrieveUser(username, usernameAuthenticationToken);
        // 验证用户信息
        additionalAuthenticationChecks(userLoginInfo, (UsernameAuthenticationToken) authentication);
        // 构造成功结果
        return createSuccessAuthentication(usernameAuthenticationToken, userLoginInfo);
    }

    @Override
    public boolean supports(@NonNull Class<?> authentication) {
        return UsernameAuthenticationToken.class.isAssignableFrom(authentication);
    }

    protected Authentication createSuccessAuthentication(Authentication authentication, UserLoginInfo userLoginInfo) {
        // 认证通过，使用 Authenticated 为 true 的构造函数
        UsernameAuthenticationToken result = UsernameAuthenticationToken.authenticated(userLoginInfo, List.of());
        // 必须转化成Map
        result.setDetails(authentication.getDetails());
        log.debug("用户名认证成功，用户: {}", userLoginInfo.getUsername());
        return result;
    }

    protected UserLoginInfo retrieveUser(String username, UsernameAuthenticationToken authentication)
            throws AuthenticationException {
        User loadedUser =
                userRepository.findByUsername(username).orElseThrow(() -> new BaseException(BaseCode.USER_NOT_FOUND));
        Collection<GrantedAuthority> authorities = new LinkedHashSet<>();
        authorities.add(FactorGrantedAuthority.fromAuthority(AUTHORITY));
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
                loadedUser.getMfaEnabled(),
                authorities);
    }

    protected void additionalAuthenticationChecks(
            UserLoginInfo userLoginInfo, UsernameAuthenticationToken authentication) throws AuthenticationException {
        String presentedPassword = authentication.getPassword();
        if (!this.passwordEncoder.matches(presentedPassword, userLoginInfo.getPassword())) {
            log.debug("身份验证失败，因为密码与存储的值不匹配");
            throw new BadCredentialsException(
                    this.messages.getMessage("usernameAuthenticationProvider.badCredentials", "错误的凭证"));
        }
    }
}
