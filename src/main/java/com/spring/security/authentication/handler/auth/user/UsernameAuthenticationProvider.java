package com.spring.security.authentication.handler.auth.user;

import com.spring.security.authentication.handler.auth.UserLoginInfo;
import com.spring.security.authentication.handler.authorization.Authority;
import com.spring.security.web.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
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
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
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
        UserLoginInfo userLoginInfo = userService.loadUserByUsername(username);
        userLoginInfo.getAuthorities().add(FactorGrantedAuthority.fromAuthority(AUTHORITY));
        log.debug("用户信息查询成功，用户: {}", userLoginInfo.getUsername());
        // 验证用户信息
        String presentedPassword = usernameAuthenticationToken.getPassword();
        if (!this.passwordEncoder.matches(presentedPassword, userLoginInfo.getPassword())) {
            log.debug("身份验证失败，因为密码与存储的值不匹配");
            throw new BadCredentialsException(
                    this.messages.getMessage("usernameAuthenticationProvider.badCredentials", "错误的凭证"));
        }
        // 构造成功结果
        // 认证通过，使用 Authenticated 为 true 的构造函数
        UsernameAuthenticationToken result =
                UsernameAuthenticationToken.authenticated(userLoginInfo, userLoginInfo.getAuthorities());
        // 必须转化成Map
        result.setDetails(authentication.getDetails());
        log.debug("用户名认证成功，用户: {}", userLoginInfo.getUsername());
        return result;
    }

    @Override
    public boolean supports(@NonNull Class<?> authentication) {
        return UsernameAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
