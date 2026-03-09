package com.spring.security.authentication.handler.auth.github.login;

import com.spring.security.authentication.handler.auth.github.GitHubAuthenticationToken;
import com.spring.security.authentication.handler.auth.utils.OAuth2AuthorizationResponseUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.function.Function;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Setter
public class GitHubLoginAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    public static final String DEFAULT_FILTER_PROCESSES_URI = "/api/login/oauth2/github/callback";

    private static final String AUTHORIZATION_REQUEST_NOT_FOUND_ERROR_CODE = "authorization_request_not_found";
    private static final String CLIENT_REGISTRATION_NOT_FOUND_ERROR_CODE = "client_registration_not_found";

    private final ClientRegistrationRepository clientRegistrationRepository;

    private AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository;
    private Function<GitHubLoginAuthenticationToken, GitHubAuthenticationToken> authenticationResultConverter =
            this::createAuthenticationResult;

    public GitHubLoginAuthenticationFilter(
            ClientRegistrationRepository clientRegistrationRepository,
            AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository,
            AuthenticationManager authenticationManager,
            AuthenticationSuccessHandler authenticationSuccessHandler,
            AuthenticationFailureHandler authenticationFailureHandler) {
        super(DEFAULT_FILTER_PROCESSES_URI, authenticationManager);
        Assert.notNull(clientRegistrationRepository, "clientRegistrationRepository cannot be null");
        Assert.notNull(authorizationRequestRepository, "authorizationRequestRepository cannot be null");
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.authorizationRequestRepository = authorizationRequestRepository;
        setAuthenticationSuccessHandler(authenticationSuccessHandler);
        setAuthenticationFailureHandler(authenticationFailureHandler);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        MultiValueMap<String, String> params = OAuth2AuthorizationResponseUtils.toMultiMap(request.getParameterMap());
        if (!OAuth2AuthorizationResponseUtils.isAuthorizationResponse(params)) {
            OAuth2Error oauth2Error = new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST);
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
        }

        OAuth2AuthorizationRequest authorizationRequest =
                this.authorizationRequestRepository.removeAuthorizationRequest(request, response);

        if (authorizationRequest == null) {
            OAuth2Error oauth2Error = new OAuth2Error(AUTHORIZATION_REQUEST_NOT_FOUND_ERROR_CODE);
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
        }

        String registrationId = authorizationRequest.getAttribute(OAuth2ParameterNames.REGISTRATION_ID);
        ClientRegistration clientRegistration = this.clientRegistrationRepository.findByRegistrationId(registrationId);
        if (clientRegistration == null) {
            OAuth2Error oauth2Error = new OAuth2Error(
                    CLIENT_REGISTRATION_NOT_FOUND_ERROR_CODE,
                    "Client Registration not found with Id: " + registrationId,
                    null);
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
        }

        String redirectUri = UriComponentsBuilder.fromUriString(UrlUtils.buildFullRequestUrl(request))
                .replaceQuery(null)
                .build()
                .toUriString();

        OAuth2AuthorizationResponse authorizationResponse =
                OAuth2AuthorizationResponseUtils.convert(params, redirectUri);

        Object authenticationDetails = this.authenticationDetailsSource.buildDetails(request);

        GitHubLoginAuthenticationToken authenticationRequest = new GitHubLoginAuthenticationToken(
                clientRegistration, new OAuth2AuthorizationExchange(authorizationRequest, authorizationResponse));
        authenticationRequest.setDetails(authenticationDetails);

        log.debug("GitHub callback 认证开始, registrationId={}", registrationId);

        GitHubLoginAuthenticationToken authenticationResult =
                (GitHubLoginAuthenticationToken) this.getAuthenticationManager().authenticate(authenticationRequest);
        GitHubAuthenticationToken gitHubAuthenticationToken =
                this.authenticationResultConverter.apply(authenticationResult);
        gitHubAuthenticationToken.setDetails(authenticationResult.getDetails());
        return gitHubAuthenticationToken;
    }

    public final void setAuthorizationRequestRepository(
            AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository) {
        Assert.notNull(authorizationRequestRepository, "authorizationRequestRepository cannot be null");
        this.authorizationRequestRepository = authorizationRequestRepository;
    }

    public final void setAuthenticationResultConverter(
            Function<GitHubLoginAuthenticationToken, GitHubAuthenticationToken> authenticationResultConverter) {
        Assert.notNull(authenticationResultConverter, "authenticationResultConverter cannot be null");
        this.authenticationResultConverter = authenticationResultConverter;
    }

    private GitHubAuthenticationToken createAuthenticationResult(GitHubLoginAuthenticationToken authenticationResult) {
        return new GitHubAuthenticationToken(
                authenticationResult.getCurrentUser(),
                authenticationResult.getAuthorities(),
                authenticationResult.getClientRegistration().getRegistrationId());
    }
}
