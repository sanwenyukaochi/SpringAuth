package org.oauth.client.authentication.github.login;

import java.util.Collection;
import java.util.Collections;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.Assert;

@Setter
@Getter
public class GitHubOAuth2LoginAuthenticationToken extends AbstractAuthenticationToken {

    private OAuth2User currentUser;
    private ClientRegistration clientRegistration;
    private OAuth2AuthorizationExchange authorizationExchange;
    private OAuth2AccessToken accessToken;
    private OAuth2RefreshToken refreshToken;

    public GitHubOAuth2LoginAuthenticationToken(
            ClientRegistration clientRegistration, OAuth2AuthorizationExchange authorizationExchange) {
        super(Collections.emptyList());
        Assert.notNull(clientRegistration, "clientRegistration cannot be null");
        Assert.notNull(authorizationExchange, "authorizationExchange cannot be null");
        this.clientRegistration = clientRegistration;
        this.authorizationExchange = authorizationExchange;
        this.setAuthenticated(false);
    }

    public GitHubOAuth2LoginAuthenticationToken(
            ClientRegistration clientRegistration,
            OAuth2AuthorizationExchange authorizationExchange,
            OAuth2User currentUser,
            Collection<? extends GrantedAuthority> authorities,
            OAuth2AccessToken accessToken,
            @Nullable OAuth2RefreshToken refreshToken) {
        super(authorities);
        Assert.notNull(clientRegistration, "clientRegistration cannot be null");
        Assert.notNull(authorizationExchange, "authorizationExchange cannot be null");
        Assert.notNull(currentUser, "currentUser cannot be null");
        Assert.notNull(accessToken, "accessToken cannot be null");
        this.clientRegistration = clientRegistration;
        this.authorizationExchange = authorizationExchange;
        this.currentUser = currentUser;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        super.setAuthenticated(true);
    }

    @Override
    public @Nullable Object getCredentials() {
        return "";
    }

    @Override
    public @Nullable Object getPrincipal() {
        return currentUser;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        Assert.isTrue(!isAuthenticated, "无法将此令牌设置为受信任令牌 - 请改用接受 GrantedAuthority 列表的构造函数。");
        super.setAuthenticated(false);
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
    }
}
