package com.spring.security.authentication.handler.auth.github.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GitHubOAuth2Service {

    private final ClientRegistrationRepository registrationRepository;

    public OAuth2User authenticateByCode(String code) {
        ClientRegistration registration = registrationRepository.findByRegistrationId("github");
        OAuth2AuthorizationExchange authorizationExchange = new OAuth2AuthorizationExchange(
                OAuth2AuthorizationRequest.authorizationCode()
                        .authorizationUri(registration.getProviderDetails().getAuthorizationUri())
                        .clientId(registration.getClientId())
                        .redirectUri(registration.getRedirectUri())
                        .scopes(registration.getScopes())
                        .state("state")
                        .build(),
                OAuth2AuthorizationResponse.success(code)
                        .redirectUri(registration.getRedirectUri())
                        .build());

        OAuth2AccessTokenResponse tokenResponse = new RestClientAuthorizationCodeTokenResponseClient()
                .getTokenResponse(new OAuth2AuthorizationCodeGrantRequest(registration, authorizationExchange));

        OAuth2UserRequest userRequest = new OAuth2UserRequest(registration, tokenResponse.getAccessToken());
        return new DefaultOAuth2UserService().loadUser(userRequest);
    }
}
