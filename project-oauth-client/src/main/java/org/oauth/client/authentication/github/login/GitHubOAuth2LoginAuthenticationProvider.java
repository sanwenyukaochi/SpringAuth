package org.oauth.client.authentication.github.login;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.oauth.client.authentication.github.authentication.GitHubOAuth2AuthorizationCodeAuthenticationProvider;
import org.oauth.client.authentication.github.authentication.GitHubOAuth2AuthorizationCodeAuthenticationToken;
import org.oauth.client.authentication.github.dto.GitHubOAuth2Meta;
import org.oauth.client.service.GitHubOAuth2UserService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Slf4j
@Setter
@Getter
@Component
public class GitHubOAuth2LoginAuthenticationProvider implements AuthenticationProvider {

    private final GitHubOAuth2AuthorizationCodeAuthenticationProvider authorizationCodeAuthenticationProvider;
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> gitHubOAuth2UserService;
    private GrantedAuthoritiesMapper authoritiesMapper = authorities -> authorities;

    public GitHubOAuth2LoginAuthenticationProvider(
            GitHubOAuth2AuthorizationCodeAuthenticationProvider authorizationCodeAuthenticationProvider,
            GitHubOAuth2UserService gitHubOAuth2UserService) {
        Assert.notNull(
                authorizationCodeAuthenticationProvider, "authorizationCodeAuthenticationProvider cannot be null");
        Assert.notNull(gitHubOAuth2UserService, "userService cannot be null");
        this.authorizationCodeAuthenticationProvider = authorizationCodeAuthenticationProvider;
        this.gitHubOAuth2UserService = gitHubOAuth2UserService;
    }

    @Override
    public Authentication authenticate(@NonNull Authentication authentication) throws AuthenticationException {
        GitHubOAuth2LoginAuthenticationToken loginAuthenticationToken =
                (GitHubOAuth2LoginAuthenticationToken) authentication;
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
        Long providerUserId = extractProviderUserId(oauth2User);

        GitHubOAuth2LoginAuthenticationToken result = new GitHubOAuth2LoginAuthenticationToken(
                loginAuthenticationToken.getClientRegistration(),
                loginAuthenticationToken.getAuthorizationExchange(),
                oauth2User,
                mappedAuthorities,
                authorizationCodeAuthenticationToken.getAccessToken(),
                authorizationCodeAuthenticationToken.getRefreshToken());
        result.setDetails(new GitHubOAuth2Meta(
                providerUserId,
                oauth2User.getAttribute("login"),
                oauth2User.getAttribute("name"),
                oauth2User.getAttribute("email")));
        log.debug("GitHub 用户信息获取成功，用户: {}", oauth2User.getAttribute("login"));
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

    @Override
    public boolean supports(@NonNull Class<?> authentication) {
        return GitHubOAuth2LoginAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
