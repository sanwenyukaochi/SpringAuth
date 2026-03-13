package com.spring.security.authentication.handler.auth.user;

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
 * 帐号密码登录认证
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UsernameAuthenticationProvider implements AuthenticationProvider {
    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JsonMapper jsonMapper;

    @Override
    public Authentication authenticate(@NonNull Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(
                UsernameAuthenticationToken.class,
                authentication,
                () -> this.messages.getMessage("UsernameAuthenticationProvider.onlySupports", "仅支持用户名身份验证提供程序"));
        UsernameAuthenticationToken usernameAuthenticationToken = (UsernameAuthenticationToken) authentication;
        // 获取用户提交的用户名
        String username = (usernameAuthenticationToken.getUsername() == null
                ? "NONE_PROVIDED"
                : usernameAuthenticationToken.getUsername());
        // 查询用户信息
        User user = retrieveUser(username, usernameAuthenticationToken);
        // 验证用户信息
        additionalAuthenticationChecks(user, (UsernameAuthenticationToken) authentication);
        // 构造成功结果
        return createSuccessAuthentication(usernameAuthenticationToken, user);
    }

    @Override
    public boolean supports(@NonNull Class<?> authentication) {
        return UsernameAuthenticationToken.class.isAssignableFrom(authentication);
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
        UsernameAuthenticationToken result = UsernameAuthenticationToken.authenticated(userLoginInfo, List.of());
        // 必须转化成Map
        result.setDetails(jsonMapper.convertValue(authentication.getDetails(), Map.class));
        log.debug("用户名认证成功，用户: {}", userLoginInfo.getUsername());
        return result;
    }

    protected User retrieveUser(String username, UsernameAuthenticationToken authentication)
            throws AuthenticationException {
        User loadedUser =
                userRepository.findByUsername(username).orElseThrow(() -> new BaseException(BaseCode.USER_NOT_FOUND));
        authentication.setDetails(null);
        log.debug("用户信息查询成功，用户: {}", loadedUser.getUsername());
        return loadedUser;
    }

    protected void additionalAuthenticationChecks(User user, UsernameAuthenticationToken authentication)
            throws AuthenticationException {
        String presentedPassword = authentication.getPassword();
        if (!this.passwordEncoder.matches(presentedPassword, user.getPassword())) {
            log.debug("身份验证失败，因为密码与存储的值不匹配");
            throw new BadCredentialsException(
                    this.messages.getMessage("usernameAuthenticationProvider.badCredentials", "错误的凭证"));
        }
    }
}
