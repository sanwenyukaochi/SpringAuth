package com.spring.security.authentication.handler.auth.email;

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
 * 邮箱密码登录认证
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailAuthenticationProvider implements AuthenticationProvider {
    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private static final String AUTHORITY = Authority.EMAIL_AUTHORITY;

    @Override
    public Authentication authenticate(@NonNull Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(
                EmailAuthenticationToken.class,
                authentication,
                () -> this.messages.getMessage("EmailAuthenticationProvider.onlySupports", "仅支持邮箱身份验证提供程序"));
        EmailAuthenticationToken emailAuthenticationToken = (EmailAuthenticationToken) authentication;
        // 获取用户提交的邮箱
        String email =
                (emailAuthenticationToken.getEmail() == null ? "NONE_PROVIDED" : emailAuthenticationToken.getEmail());
        // 查询用户信息
        UserLoginInfo userLoginInfo = userService.loadUserByEmail(email);
        userLoginInfo.getAuthorities().add(FactorGrantedAuthority.fromAuthority(AUTHORITY));
        log.debug("用户信息查询成功，用户: {}", userLoginInfo.getUsername());
        // 验证用户信息
        String presentedPassword = emailAuthenticationToken.getPassword();
        if (!this.passwordEncoder.matches(presentedPassword, userLoginInfo.getPassword())) {
            log.debug("身份验证失败，因为验证码与存储的值不匹配");
            throw new BadCredentialsException(
                    this.messages.getMessage("emailAuthenticationProvider.badCredentials", "错误的凭证"));
        }
        // 构造成功结果
        // 认证通过，使用 Authenticated 为 true 的构造函数
        EmailAuthenticationToken result =
                EmailAuthenticationToken.authenticated(userLoginInfo, userLoginInfo.getAuthorities());
        // 必须转化成Map
        result.setDetails(authentication.getDetails());
        log.debug("邮箱认证成功，用户: {}", userLoginInfo.getUsername());
        return result;
    }

    @Override
    public boolean supports(@NonNull Class<?> authentication) {
        return EmailAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
