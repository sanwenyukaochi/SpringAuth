package com.spring.security.authentication.handler.auth.github;

import com.spring.security.authentication.handler.auth.github.repository.GitHubHttpSessionOAuth2AuthorizationRequestRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class GitHubOAuth2AuthorizationRedirectFilter extends OncePerRequestFilter {

    public static final String DEFAULT_AUTHORIZATION_REQUEST_BASE_URI = "/api/login/oauth";
    private static final RequestMatcher AUTHORIZATION_REQUEST_MATCHER = PathPatternRequestMatcher.withDefaults()
            .matcher(DEFAULT_AUTHORIZATION_REQUEST_BASE_URI + "/{registrationId}");

    private final RedirectStrategy authorizationRedirectStrategy = new DefaultRedirectStrategy();

    private final OAuth2AuthorizationRequestResolver authorizationRequestResolver;

    private final AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository;

    public GitHubOAuth2AuthorizationRedirectFilter(
            ClientRegistrationRepository clientRegistrationRepository,
            GitHubHttpSessionOAuth2AuthorizationRequestRepository authorizationRequestRepository) {
        this(
                new DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository, DEFAULT_AUTHORIZATION_REQUEST_BASE_URI),
                authorizationRequestRepository);
    }

    public GitHubOAuth2AuthorizationRedirectFilter(
            OAuth2AuthorizationRequestResolver authorizationRequestResolver,
            AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository) {
        Assert.notNull(authorizationRequestResolver, "authorizationRequestResolver cannot be null");
        Assert.notNull(authorizationRequestRepository, "authorizationRequestRepository cannot be null");
        this.authorizationRequestResolver = authorizationRequestResolver;
        this.authorizationRequestRepository = authorizationRequestRepository;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !AUTHORIZATION_REQUEST_MATCHER.matches(request);
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        OAuth2AuthorizationRequest authorizationRequest;
        try {
            authorizationRequest = this.authorizationRequestResolver.resolve(request);
        } catch (Exception ex) {
            log.error("解析 GitHub OAuth2AuthorizationRequest 失败", ex);
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "GitHub authorization request resolve failed");
            return;
        }

        if (authorizationRequest == null) {
            filterChain.doFilter(request, response);
            return;
        }

        sendRedirectForAuthorization(request, response, authorizationRequest);
    }

    private void sendRedirectForAuthorization(
            HttpServletRequest request, HttpServletResponse response, OAuth2AuthorizationRequest authorizationRequest)
            throws IOException {

        if (AuthorizationGrantType.AUTHORIZATION_CODE.equals(authorizationRequest.getGrantType())) {
            this.authorizationRequestRepository.saveAuthorizationRequest(authorizationRequest, request, response);
        }

        log.debug(
                "GitHub OAuth2 授权请求已保存并准备跳转, state={}, redirectUri={}",
                authorizationRequest.getState(),
                authorizationRequest.getRedirectUri());

        this.authorizationRedirectStrategy.sendRedirect(
                request, response, authorizationRequest.getAuthorizationRequestUri());
    }
}
