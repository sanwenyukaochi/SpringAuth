package com.spring.security.authentication.handler.auth.email;

import com.spring.security.authentication.handler.auth.UserLoginInfo;
import com.spring.security.common.web.enums.BaseCode;
import com.spring.security.common.web.exception.BaseException;
import com.spring.security.domain.model.entity.User;
import com.spring.security.domain.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import tools.jackson.databind.json.JsonMapper;

/**
 * 邮箱密码登录认证
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailAuthenticationProvider implements AuthenticationProvider {
    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JsonMapper jsonMapper;

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
        User user = retrieveUser(email, emailAuthenticationToken);
        // 验证用户信息
        additionalAuthenticationChecks(user, (EmailAuthenticationToken) authentication);
        // 构造成功结果
        return createSuccessAuthentication(emailAuthenticationToken, user);
    }

    @Override
    public boolean supports(@NonNull Class<?> authentication) {
        return EmailAuthenticationToken.class.isAssignableFrom(authentication);
    }

    protected Authentication createSuccessAuthentication(Authentication authentication, User user) {
        UserLoginInfo userLoginInfo = new UserLoginInfo(
                UUID.randomUUID().toString(),
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getPhone(),
                user.getEmail(),
                user.getAccountNonLocked(),
                user.getAccountNonLocked(),
                user.getCredentialsNonExpired(),
                user.getEnabled(),
                user.getTwoFactorSecret(),
                user.getTwoFactorEnabled());
        // 认证通过，使用 Authenticated 为 true 的构造函数
        EmailAuthenticationToken result = EmailAuthenticationToken.authenticated(userLoginInfo, List.of());
        // 必须转化成Map
        result.setDetails(jsonMapper.convertValue(authentication.getDetails(), Map.class));
        log.debug("邮箱认证成功，用户: {}", userLoginInfo.getUsername());
        return result;
    }

    protected User retrieveUser(String email, EmailAuthenticationToken authentication) throws AuthenticationException {
        User loadedUser =
                userRepository.findByEmail(email).orElseThrow(() -> new BaseException(BaseCode.USER_EMAIL_NOT_FOUND));
        authentication.setDetails(null);
        log.debug("用户信息查询成功，用户: {}", loadedUser.getUsername());
        return loadedUser;
    }

    protected void additionalAuthenticationChecks(User user, EmailAuthenticationToken authentication)
            throws AuthenticationException {
        String presentedPassword = authentication.getPassword();
        if (!this.passwordEncoder.matches(presentedPassword, user.getPassword())) {
            log.debug("身份验证失败，因为验证码与存储的值不匹配");
            throw new BadCredentialsException(
                    this.messages.getMessage("emailAuthenticationProvider.badCredentials", "错误的凭证"));
        }
    }
}
