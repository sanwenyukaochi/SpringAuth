package com.spring.security.authentication.handler.auth.github.authentication;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.util.Assert;

@Setter
@Getter
// 对应 OAuth2AuthorizationCodeAuthenticationToken
public class GitHubAuthorizationCodeAuthenticationToken extends AbstractAuthenticationToken {

    private Map<String, Object> additionalParameters = new HashMap<>();
    private ClientRegistration clientRegistration;
    private OAuth2AuthorizationExchange authorizationExchange;
    private OAuth2AccessToken accessToken;
    private OAuth2RefreshToken refreshToken;

    public GitHubAuthorizationCodeAuthenticationToken(
            ClientRegistration clientRegistration, OAuth2AuthorizationExchange authorizationExchange) {
        super(Collections.emptyList());
        Assert.notNull(clientRegistration, "clientRegistration cannot be null");
        Assert.notNull(authorizationExchange, "authorizationExchange cannot be null");
        this.clientRegistration = clientRegistration;
        this.authorizationExchange = authorizationExchange;
        this.setAuthenticated(false);
    }

    public GitHubAuthorizationCodeAuthenticationToken(
            ClientRegistration clientRegistration,
            OAuth2AuthorizationExchange authorizationExchange,
            OAuth2AccessToken accessToken,
            OAuth2RefreshToken refreshToken,
            Map<String, Object> additionalParameters) {
        this(clientRegistration, authorizationExchange);
        Assert.notNull(accessToken, "accessToken cannot be null");
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.setAuthenticated(true);
        this.additionalParameters.putAll(additionalParameters);
    }

    @Override
    public Object getPrincipal() {
        return this.clientRegistration.getClientId();
    }

    @Override
    public Object getCredentials() {
        return (this.accessToken != null)
                ? this.accessToken.getTokenValue()
                : this.authorizationExchange.getAuthorizationResponse().getCode();
    }
}
