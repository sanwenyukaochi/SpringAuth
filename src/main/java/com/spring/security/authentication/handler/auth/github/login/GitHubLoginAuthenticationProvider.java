package com.spring.security.authentication.handler.auth.github.login;

import com.spring.security.authentication.handler.auth.UserLoginInfo;
import com.spring.security.authentication.handler.auth.github.authentication.GitHubAuthorizationCodeAuthenticationProvider;
import com.spring.security.authentication.handler.auth.github.authentication.GitHubAuthorizationCodeAuthenticationToken;
import com.spring.security.authentication.handler.auth.github.service.GitHubUserService;
import com.spring.security.domain.model.entity.User;
import com.spring.security.domain.model.entity.UserIdentity;
import com.spring.security.domain.repository.UserIdentityRepository;
import com.spring.security.domain.repository.UserRepository;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Setter
@Getter
@Component
public class GitHubLoginAuthenticationProvider implements AuthenticationProvider {

    private final GitHubAuthorizationCodeAuthenticationProvider authorizationCodeAuthenticationProvider;

    private final GitHubUserService gitHubUserService;

    private final UserIdentityRepository userIdentityRepository;

    private final UserRepository userRepository;

    private GrantedAuthoritiesMapper authoritiesMapper = (authorities) -> authorities;

    public GitHubLoginAuthenticationProvider(
            GitHubAuthorizationCodeAuthenticationProvider authorizationCodeAuthenticationProvider,
            GitHubUserService gitHubUserService,
            UserIdentityRepository userIdentityRepository,
            UserRepository userRepository) {
        Assert.notNull(
                authorizationCodeAuthenticationProvider, "authorizationCodeAuthenticationProvider cannot be null");
        Assert.notNull(gitHubUserService, "userService cannot be null");
        Assert.notNull(userIdentityRepository, "userIdentityRepository cannot be null");
        Assert.notNull(userRepository, "userRepository cannot be null");
        this.authorizationCodeAuthenticationProvider = authorizationCodeAuthenticationProvider;
        this.gitHubUserService = gitHubUserService;
        this.userIdentityRepository = userIdentityRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        GitHubLoginAuthenticationToken loginAuthenticationToken = (GitHubLoginAuthenticationToken) authentication;

        GitHubAuthorizationCodeAuthenticationToken authorizationCodeAuthenticationToken =
                new GitHubAuthorizationCodeAuthenticationToken(
                        loginAuthenticationToken.getClientRegistration(),
                        loginAuthenticationToken.getAuthorizationExchange());
        authorizationCodeAuthenticationToken.setDetails(loginAuthenticationToken.getDetails());

        GitHubAuthorizationCodeAuthenticationToken authorizationCodeAuthenticationResult =
                (GitHubAuthorizationCodeAuthenticationToken)
                        this.authorizationCodeAuthenticationProvider.authenticate(authorizationCodeAuthenticationToken);

        OAuth2AccessToken accessToken = authorizationCodeAuthenticationResult.getAccessToken();
        OAuth2RefreshToken refreshToken = authorizationCodeAuthenticationResult.getRefreshToken();
        Map<String, Object> additionalParameters = authorizationCodeAuthenticationResult.getAdditionalParameters();

        // 2. 再用 access_token 拉取 GitHub 用户信息
        OAuth2User oauth2User = this.gitHubUserService.loadUser(new OAuth2UserRequest(
                loginAuthenticationToken.getClientRegistration(), accessToken, additionalParameters));

        Collection<GrantedAuthority> authorities = new HashSet<>(oauth2User.getAuthorities());
        Collection<GrantedAuthority> mappedAuthorities =
                new LinkedHashSet<>(this.authoritiesMapper.mapAuthorities(authorities));
        Long providerUserId = extractProviderUserId(oauth2User);
        Optional<UserIdentity> userIdentity =
                userIdentityRepository.findByProviderUserIdAndProvider(providerUserId, UserIdentity.Provider.GITHUB);
        UserLoginInfo currentUser = userIdentity
                .map(UserIdentity::getUserId)
                .flatMap(userRepository::findById)
                .map(this::toUserLoginInfo)
                .orElseGet(() -> buildUnboundGitHubUser(oauth2User, providerUserId));

        Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("provider", "github");
        additionalInfo.put("providerUserId", providerUserId);
        additionalInfo.put("login", oauth2User.getAttribute("login"));
        additionalInfo.put("name", oauth2User.getAttribute("name"));
        additionalInfo.put("email", oauth2User.getAttribute("email"));
        additionalInfo.put("isNewUser", userIdentity.isEmpty());

        GitHubLoginAuthenticationToken authenticationResult = new GitHubLoginAuthenticationToken(
                loginAuthenticationToken.getClientRegistration(),
                loginAuthenticationToken.getAuthorizationExchange(),
                currentUser,
                mappedAuthorities,
                accessToken,
                refreshToken);
        authenticationResult.setDetails(additionalInfo);
        return authenticationResult;
    }

    private Long extractProviderUserId(OAuth2User oauth2User) {
        Object id = oauth2User.getAttribute("id");
        Assert.notNull(id, "GitHub user id cannot be null");
        if (id instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(id));
    }

    private UserLoginInfo toUserLoginInfo(User user) {
        return new UserLoginInfo(
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
                user.getTwoFactorSecret(),
                user.getTwoFactorEnabled());
    }

    private UserLoginInfo buildUnboundGitHubUser(OAuth2User oauth2User, Long providerUserId) {
        String username = Optional.ofNullable(oauth2User.<String>getAttribute("login"))
                .filter(login -> !login.isBlank())
                .orElse("github_" + providerUserId);
        return new UserLoginInfo(
                UUID.randomUUID().toString(),
                null,
                username,
                null,
                null,
                oauth2User.getAttribute("email"),
                true,
                true,
                true,
                true,
                null,
                false);
    }

    public final void setAuthoritiesMapper(GrantedAuthoritiesMapper authoritiesMapper) {
        Assert.notNull(authoritiesMapper, "authoritiesMapper cannot be null");
        this.authoritiesMapper = authoritiesMapper;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return GitHubLoginAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
