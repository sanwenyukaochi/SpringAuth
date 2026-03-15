package com.spring.security.authentication.handler.auth.github.login;

import com.spring.security.authentication.handler.auth.UserLoginInfo;
import com.spring.security.authentication.handler.auth.github.authentication.GitHubOAuth2AuthorizationCodeAuthenticationProvider;
import com.spring.security.authentication.handler.auth.github.authentication.GitHubOAuth2AuthorizationCodeAuthenticationToken;
import com.spring.security.authentication.handler.auth.github.dto.GitHubOAuth2Meta;
import com.spring.security.authentication.handler.auth.github.service.GitHubOAuth2UserService;
import com.spring.security.authentication.handler.authorization.Authority;
import com.spring.security.web.model.entity.User;
import com.spring.security.web.model.entity.UserIdentity;
import com.spring.security.web.repository.UserIdentityRepository;
import com.spring.security.web.repository.UserRepository;
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
    private final GitHubOAuth2AuthorizationCodeAuthenticationProvider authorizationCodeAuthenticationProvider;
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> gitHubOAuth2UserService;
    private final UserIdentityRepository userIdentityRepository;
    private final UserRepository userRepository;
    private GrantedAuthoritiesMapper authoritiesMapper = (authorities) -> authorities;
    private final JsonMapper jsonMapper;
    private static final String AUTHORITY = Authority.AUTHORIZATION_CODE_AUTHORITY;

    public GitHubOAuth2LoginAuthenticationProvider(
            GitHubOAuth2AuthorizationCodeAuthenticationProvider authorizationCodeAuthenticationProvider,
            GitHubOAuth2UserService gitHubOAuth2UserService,
            UserIdentityRepository userIdentityRepository,
            UserRepository userRepository,
            JsonMapper jsonMapper) {
        Assert.notNull(
                authorizationCodeAuthenticationProvider, "authorizationCodeAuthenticationProvider cannot be null");
        Assert.notNull(gitHubOAuth2UserService, "userService cannot be null");
        Assert.notNull(userIdentityRepository, "userIdentityRepository cannot be null");
        Assert.notNull(userRepository, "userRepository cannot be null");
        this.authorizationCodeAuthenticationProvider = authorizationCodeAuthenticationProvider;
        this.gitHubOAuth2UserService = gitHubOAuth2UserService;
        this.userIdentityRepository = userIdentityRepository;
        this.userRepository = userRepository;
        this.jsonMapper = jsonMapper;
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
        User loadedUser = retrieveUser(providerUserId);

        // 认证通过，使用 Authenticated 为 true 的构造函数
        // 验证用户信息
        // 构造成功结果
        GitHubOAuth2LoginAuthenticationToken result = new GitHubOAuth2LoginAuthenticationToken(
                loginAuthenticationToken.getClientRegistration(),
                loginAuthenticationToken.getAuthorizationExchange(),
                Optional.ofNullable(loadedUser)
                        .map(user -> new UserLoginInfo(
                                UUID.randomUUID().toString(),
                                user.getId(),
                                user.getUsername(),
                                user.getPassword(),
                                user.getPhone(),
                                user.getEmail(),
                                user.getAccountNonLocked(),
                                user.getAccountNonExpired(),
                                user.getCredentialsNonExpired(),
                                user.getEnabled(),
                                user.getMfaSecret(),
                                user.getMfaEnabled(),
                                authorities))
                        .orElse(null),
                mappedAuthorities,
                authorizationCodeAuthenticationToken.getAccessToken(),
                authorizationCodeAuthenticationToken.getRefreshToken());
        result.setDetails(new GitHubOAuth2Meta(
                UserIdentity.Provider.GITHUB,
                providerUserId,
                oauth2User.getAttribute("login"),
                oauth2User.getAttribute("name"),
                oauth2User.getAttribute("email"),
                loadedUser == null ? Boolean.TRUE : Boolean.FALSE));
        log.debug("用户名认证成功，用户: {}", Optional.ofNullable(oauth2User.getAttribute("login")));
        return result;
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
    //    protected Authentication createSuccessAuthentication(
    //            Authentication authentication, User loadedUser, Collection<GrantedAuthority> mappedAuthorities) {
    //        GitHubOAuth2AuthorizationCodeAuthenticationToken authorizationCodeAuthenticationToken =
    //                (GitHubOAuth2AuthorizationCodeAuthenticationToken) authentication;
    //        GitHubOAuth2LoginAuthenticationToken result = new GitHubOAuth2LoginAuthenticationToken(
    //                authorizationCodeAuthenticationToken.getClientRegistration(),
    //                authorizationCodeAuthenticationToken.getAuthorizationExchange(),
    //                Optional.ofNullable(loadedUser)
    //                        .map(user -> new UserLoginInfo(
    //                                UUID.randomUUID().toString(),
    //                                user.getId(),
    //                                user.getUsername(),
    //                                user.getPassword(),
    //                                user.getPhone(),
    //                                user.getEmail(),
    //                                user.getAccountNonLocked(),
    //                                user.getAccountNonExpired(),
    //                                user.getCredentialsNonExpired(),
    //                                user.getEnabled(),
    //                                user.getMfaSecret(),
    //                                user.getMfaEnabled(),
    //                                authorities))
    //                        .orElse(null),
    //                mappedAuthorities,
    //                authorizationCodeAuthenticationToken.getAccessToken(),
    //                authorizationCodeAuthenticationToken.getRefreshToken());
    //        result.setDetails(new GitHubOAuth2Meta(
    //                UserIdentity.Provider.GITHUB,
    //                providerUserId,
    //                oauth2User.getAttribute("login"),
    //                oauth2User.getAttribute("name"),
    //                oauth2User.getAttribute("email"),
    //                loadedUser == null ? Boolean.TRUE:Boolean.FALSE));
    //        log.debug("用户名认证成功，用户: {}", Optional.ofNullable(oauth2User.getAttribute("login")));
    //        return result;
    //    }
    protected User retrieveUser(Long providerUserId) throws AuthenticationException {
        log.debug("查询GitHub用户: providerUserId={}", providerUserId);
        return userIdentityRepository
                .findByProviderUserIdAndProvider(providerUserId, UserIdentity.Provider.GITHUB)
                .map(UserIdentity::getUserId)
                .flatMap(userRepository::findById)
                .orElse(null);
    }
}
