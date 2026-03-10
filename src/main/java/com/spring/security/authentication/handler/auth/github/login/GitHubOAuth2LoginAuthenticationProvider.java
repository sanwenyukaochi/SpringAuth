package com.spring.security.authentication.handler.auth.github.login;

import com.spring.security.authentication.handler.auth.UserLoginInfo;
import com.spring.security.authentication.handler.auth.github.authentication.GitHubOAuth2AuthorizationCodeAuthenticationProvider;
import com.spring.security.authentication.handler.auth.github.authentication.GitHubOAuth2AuthorizationCodeAuthenticationToken;
import com.spring.security.authentication.handler.auth.github.dto.GitHubOAuth2Meta;
import com.spring.security.authentication.handler.auth.github.service.GitHubOAuth2UserService;
import com.spring.security.domain.model.entity.User;
import com.spring.security.domain.model.entity.UserIdentity;
import com.spring.security.domain.repository.UserIdentityRepository;
import com.spring.security.domain.repository.UserRepository;
import java.util.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Setter
@Getter
@Component
public class GitHubOAuth2LoginAuthenticationProvider implements AuthenticationProvider {
    private static final String AUTHORITY = FactorGrantedAuthority.AUTHORIZATION_CODE_AUTHORITY;
    private final GitHubOAuth2AuthorizationCodeAuthenticationProvider authorizationCodeAuthenticationProvider;
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> gitHubOAuth2UserService;
    private final UserIdentityRepository userIdentityRepository;
    private final UserRepository userRepository;
    private GrantedAuthoritiesMapper authoritiesMapper = (authorities) -> authorities;

    public GitHubOAuth2LoginAuthenticationProvider(
            GitHubOAuth2AuthorizationCodeAuthenticationProvider authorizationCodeAuthenticationProvider,
            GitHubOAuth2UserService gitHubOAuth2UserService,
            UserIdentityRepository userIdentityRepository,
            UserRepository userRepository) {
        Assert.notNull(
                authorizationCodeAuthenticationProvider, "authorizationCodeAuthenticationProvider cannot be null");
        Assert.notNull(gitHubOAuth2UserService, "userService cannot be null");
        Assert.notNull(userIdentityRepository, "userIdentityRepository cannot be null");
        Assert.notNull(userRepository, "userRepository cannot be null");
        this.authorizationCodeAuthenticationProvider = authorizationCodeAuthenticationProvider;
        this.gitHubOAuth2UserService = gitHubOAuth2UserService;
        this.userIdentityRepository = userIdentityRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Authentication authenticate(@NonNull Authentication authentication) throws AuthenticationException {
        GitHubOAuth2LoginAuthenticationToken loginAuthenticationToken =
                (GitHubOAuth2LoginAuthenticationToken) authentication;
        if (loginAuthenticationToken
                .getAuthorizationExchange()
                .getAuthorizationRequest()
                .getScopes()
                .contains("openid")) {
            return null;
        }
        GitHubOAuth2AuthorizationCodeAuthenticationToken authorizationCodeAuthenticationToken;
        try {
            authorizationCodeAuthenticationToken = (GitHubOAuth2AuthorizationCodeAuthenticationToken)
                    this.authorizationCodeAuthenticationProvider.authenticate(
                            new GitHubOAuth2AuthorizationCodeAuthenticationToken(
                                    loginAuthenticationToken.getClientRegistration(),
                                    loginAuthenticationToken.getAuthorizationExchange()));
        } catch (OAuth2AuthorizationException ex) {
            OAuth2Error oauth2Error = ex.getError();
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString(), ex);
        }
        OAuth2AccessToken accessToken = authorizationCodeAuthenticationToken.getAccessToken();
        Map<String, Object> additionalParameters = authorizationCodeAuthenticationToken.getAdditionalParameters();
        OAuth2User oauth2User = this.gitHubOAuth2UserService.loadUser(new OAuth2UserRequest(
                loginAuthenticationToken.getClientRegistration(), accessToken, additionalParameters));
        Collection<GrantedAuthority> authorities = new HashSet<>(oauth2User.getAuthorities());
        Collection<GrantedAuthority> mappedAuthorities =
                new LinkedHashSet<>(this.authoritiesMapper.mapAuthorities(authorities));
        mappedAuthorities.add(FactorGrantedAuthority.fromAuthority(AUTHORITY));
        Long providerUserId = extractProviderUserId(oauth2User);

        // 查询用户信息
        User user = retrieveUser(providerUserId, oauth2User, authorizationCodeAuthenticationToken);
        // 验证用户信息
        // 构造成功结果
        return createSuccessAuthentication(authorizationCodeAuthenticationToken, user, oauth2User, mappedAuthorities);
    }

    private Long extractProviderUserId(OAuth2User oauth2User) {
        Object id = oauth2User.getAttribute("id");
        Assert.notNull(id, "GitHub user id cannot be null");
        if (id instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(id));
    }

    public final void setAuthoritiesMapper(GrantedAuthoritiesMapper authoritiesMapper) {
        Assert.notNull(authoritiesMapper, "authoritiesMapper cannot be null");
        this.authoritiesMapper = authoritiesMapper;
    }

    @Override
    public boolean supports(@NonNull Class<?> authentication) {
        return GitHubOAuth2LoginAuthenticationToken.class.isAssignableFrom(authentication);
    }

    protected Authentication createSuccessAuthentication(
            Authentication authentication,
            User user,
            OAuth2User oauth2User,
            Collection<GrantedAuthority> mappedAuthorities) {
        GitHubOAuth2AuthorizationCodeAuthenticationToken loginAuthenticationToken =
                (GitHubOAuth2AuthorizationCodeAuthenticationToken) authentication;
        UserLoginInfo userLoginInfo = new UserLoginInfo(
                UUID.randomUUID().toString(),
                user != null ? user.getId() : null,
                user != null ? user.getUsername() : oauth2User.getAttribute("login"),
                user != null ? user.getPassword() : null,
                user != null ? user.getPhone() : null,
                user != null ? user.getEmail() : oauth2User.getAttribute("email"),
                user != null ? user.getAccountNonLocked() : null,
                user != null ? user.getAccountNonExpired() : null,
                user != null ? user.getCredentialsNonExpired() : null,
                user != null ? user.getEnabled() : null,
                user != null ? user.getTwoFactorSecret() : null,
                user != null ? user.getTwoFactorEnabled() : null);

        // 认证通过，使用 Authenticated 为 true 的构造函数
        GitHubOAuth2LoginAuthenticationToken result = new GitHubOAuth2LoginAuthenticationToken(
                loginAuthenticationToken.getClientRegistration(),
                loginAuthenticationToken.getAuthorizationExchange(),
                userLoginInfo,
                mappedAuthorities,
                loginAuthenticationToken.getAccessToken(),
                loginAuthenticationToken.getRefreshToken());
        // 必须转化成Map
        result.setDetails(JsonMapper.shared().convertValue(authentication.getDetails(), Map.class));
        log.debug("用户名认证成功，用户: {}", userLoginInfo.getUsername());
        return result;
    }

    protected User retrieveUser(
            Long providerUserId, OAuth2User oauth2User, GitHubOAuth2AuthorizationCodeAuthenticationToken authentication)
            throws AuthenticationException {
        log.debug("查询GitHub用户: providerUserId={}", providerUserId);
        return userIdentityRepository
                .findByProviderUserIdAndProvider(providerUserId, UserIdentity.Provider.GITHUB)
                .map(UserIdentity::getUserId)
                .flatMap(userRepository::findById)
                .map(user -> {
                    authentication.setDetails(new GitHubOAuth2Meta(
                            UserIdentity.Provider.GITHUB,
                            providerUserId,
                            oauth2User.getAttribute("login"),
                            oauth2User.getAttribute("name"),
                            oauth2User.getAttribute("email"),
                            Boolean.FALSE));
                    return user;
                })
                .orElseGet(() -> {
                    authentication.setDetails(new GitHubOAuth2Meta(
                            UserIdentity.Provider.GITHUB,
                            providerUserId,
                            oauth2User.getAttribute("login"),
                            oauth2User.getAttribute("name"),
                            oauth2User.getAttribute("email"),
                            Boolean.TRUE));
                    return null;
                });
    }
}
