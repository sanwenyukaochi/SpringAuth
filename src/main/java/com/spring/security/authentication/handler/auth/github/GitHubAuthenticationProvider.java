package com.spring.security.authentication.handler.auth.github;

import com.spring.security.authentication.handler.auth.UserLoginInfo;
import com.spring.security.authentication.handler.auth.github.dto.GitHubOAuthMeta;
import com.spring.security.authentication.handler.auth.github.service.GitHubOAuth2Service;
import com.spring.security.common.web.enums.BaseCode;
import com.spring.security.common.web.exception.BaseException;
import com.spring.security.domain.model.entity.User;
import com.spring.security.domain.model.entity.UserIdentity;
import com.spring.security.domain.repository.UserIdentityRepository;
import com.spring.security.domain.repository.UserRepository;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import tools.jackson.databind.json.JsonMapper;

/**
 * GITHUB认证提供者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubAuthenticationProvider implements AuthenticationProvider {
    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
    private final UserIdentityRepository userIdentityRepository;
    private final GitHubOAuth2Service githubOAuth2Service;
    private final UserRepository userRepository;

    @Override
    public Authentication authenticate(@NonNull Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(
                GitHubAuthenticationToken.class,
                authentication,
                () -> this.messages.getMessage("GitHubAuthenticationProvider.onlySupports", "仅支持GitHub身份验证提供程序"));
        GitHubAuthenticationToken gitHubAuthenticationToken = (GitHubAuthenticationToken) authentication;
        // 获取用户提交的手机号
        String code =
                (gitHubAuthenticationToken.getCode() == null ? "NONE_PROVIDED" : gitHubAuthenticationToken.getCode());
        // 查询用户信息
        User user = retrieveUser(code, gitHubAuthenticationToken);
        // 验证用户信息
        additionalAuthenticationChecks(user, (GitHubAuthenticationToken) authentication);
        // 构造成功结果
        return createSuccessAuthentication(gitHubAuthenticationToken, user);
    }

    @Override
    public boolean supports(@NonNull Class<?> authentication) {
        return GitHubAuthenticationToken.class.isAssignableFrom(authentication);
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
        GitHubAuthenticationToken result = new GitHubAuthenticationToken(userLoginInfo, List.of());
        // 必须转化成Map
        result.setDetails(JsonMapper.shared().convertValue(authentication.getDetails(), Map.class));
        log.debug("GitHub认证成功，用户: {}", userLoginInfo.getUsername());
        return result;
    }

    protected User retrieveUser(String code, GitHubAuthenticationToken authentication) throws AuthenticationException {
        OAuth2User oAuth2User = githubOAuth2Service.authenticateByCode(code);
        Long providerUserId = Optional.ofNullable(oAuth2User.getAttribute("id"))
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .map(Number::longValue)
                .orElseThrow(() -> new BaseException(BaseCode.USER_NOT_FOUND));
        User loadedUser = userIdentityRepository
                .findByProviderUserIdAndProvider(providerUserId, UserIdentity.AuthProvider.GITHUB)
                .flatMap(identity -> userRepository.findById(identity.getUser().getId()))
                .orElseGet(() -> {
                    User user = new User();
                    user.setUsername(oAuth2User.getAttribute("login"));
                    return user;
                });
        boolean isNewUser = loadedUser.getId() == null;
        authentication.setDetails(new GitHubOAuthMeta(isNewUser, providerUserId));
        log.debug("用户信息查询{}，用户: {}", !isNewUser ? "成功" : "失败", loadedUser.getUsername());
        return loadedUser;
    }

    protected void additionalAuthenticationChecks(User user, GitHubAuthenticationToken authentication)
            throws AuthenticationException {
        String presentedCode = authentication.getCode();
        if (presentedCode == null || user == null) {
            log.debug("身份验证失败，因为身份与数据库存储的值不匹配");
            throw new BadCredentialsException(
                    this.messages.getMessage("gitHubAuthenticationProvider.sessionExpired", "错误的凭证"));
        }
    }
}
