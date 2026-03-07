package com.spring.security.authentication.handler.auth.github;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Setter
public class GitHubAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final RequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER =
            PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/api/login/oauth/github");

    private boolean postOnly = true;

    public GitHubAuthenticationFilter(
            AuthenticationManager authenticationManager,
            AuthenticationSuccessHandler authenticationSuccessHandler,
            AuthenticationFailureHandler authenticationFailureHandler) {
        super(DEFAULT_ANT_PATH_REQUEST_MATCHER, authenticationManager);
        setAuthenticationSuccessHandler(authenticationSuccessHandler);
        setAuthenticationFailureHandler(authenticationFailureHandler);
    }

    @Override
    public Authentication attemptAuthentication(
            @NonNull HttpServletRequest request, @NonNull HttpServletResponse response)
            throws AuthenticationException, IOException {
        log.debug("use GithubAuthenticationFilter");
        if (this.postOnly && !request.getMethod().equals(HttpMethod.POST.name())) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }
        GitHubLoginRequest gitHubLoginRequest =
                JsonMapper.shared().readValue(request.getInputStream(), GitHubLoginRequest.class);
        String code = obtainCode(gitHubLoginRequest);
        code = (code != null) ? code.trim() : "";

        // 封装成Spring Security需要的对象
        GitHubAuthenticationToken authRequest = new GitHubAuthenticationToken(code);
        // 开始登录认证。SpringSecurity会利用 Authentication对象去寻找 AuthenticationProvider进行登录认证
        return getAuthenticationManager().authenticate(authRequest);
    }

    @Nullable protected String obtainCode(GitHubLoginRequest gitHubLoginRequest) {
        return gitHubLoginRequest.code();
    }
}
