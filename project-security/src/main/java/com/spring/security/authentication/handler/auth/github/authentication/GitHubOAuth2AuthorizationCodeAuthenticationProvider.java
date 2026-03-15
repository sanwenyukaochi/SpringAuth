package com.spring.security.authentication.handler.auth.github.authentication;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * GITHUB认证提供者
 */
@Slf4j
@Component
// @RequiredArgsConstructor
// OAuth2AuthorizationCodeAuthenticationProvider
public class GitHubOAuth2AuthorizationCodeAuthenticationProvider implements AuthenticationProvider {
    private static final String INVALID_STATE_PARAMETER_ERROR_CODE = "invalid_state_parameter";

    private final OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient;

    public GitHubOAuth2AuthorizationCodeAuthenticationProvider(
            OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient) {
        Assert.notNull(accessTokenResponseClient, "accessTokenResponseClient cannot be null");
        this.accessTokenResponseClient = accessTokenResponseClient;
    }

    @Override
    public Authentication authenticate(@NonNull Authentication authentication) throws AuthenticationException {
        GitHubOAuth2AuthorizationCodeAuthenticationToken authorizationCodeAuthentication =
                (GitHubOAuth2AuthorizationCodeAuthenticationToken) authentication;
        OAuth2AuthorizationResponse authorizationResponse =
                authorizationCodeAuthentication.getAuthorizationExchange().getAuthorizationResponse();
        if (authorizationResponse.statusError()) {
            throw new OAuth2AuthorizationException(authorizationResponse.getError());
        }
        OAuth2AuthorizationRequest authorizationRequest =
                authorizationCodeAuthentication.getAuthorizationExchange().getAuthorizationRequest();
        if (!authorizationResponse.getState().equals(authorizationRequest.getState())) {
            OAuth2Error oauth2Error = new OAuth2Error(INVALID_STATE_PARAMETER_ERROR_CODE);
            throw new OAuth2AuthorizationException(oauth2Error);
        }
        OAuth2AccessTokenResponse accessTokenResponse =
                this.accessTokenResponseClient.getTokenResponse(new OAuth2AuthorizationCodeGrantRequest(
                        authorizationCodeAuthentication.getClientRegistration(),
                        authorizationCodeAuthentication.getAuthorizationExchange()));
        GitHubOAuth2AuthorizationCodeAuthenticationToken authenticationResult =
                new GitHubOAuth2AuthorizationCodeAuthenticationToken(
                        authorizationCodeAuthentication.getClientRegistration(),
                        authorizationCodeAuthentication.getAuthorizationExchange(),
                        accessTokenResponse.getAccessToken(),
                        accessTokenResponse.getRefreshToken(),
                        accessTokenResponse.getAdditionalParameters());
        authenticationResult.setDetails(authorizationCodeAuthentication.getDetails());
        return authenticationResult;
    }

    @Override
    public boolean supports(@NonNull Class<?> authentication) {
        return GitHubOAuth2AuthorizationCodeAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
