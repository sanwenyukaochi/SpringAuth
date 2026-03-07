package com.spring.security.authentication.handler.auth.email;

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
public class EmailAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final RequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER =
            PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/api/login/application/email");

    private boolean postOnly = true;

    public EmailAuthenticationFilter(
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
        log.debug("use EmailAuthenticationFilter");
        if (this.postOnly && !request.getMethod().equals(HttpMethod.POST.name())) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }
        // 提取请求数据
        EmailLoginRequest emailLoginRequest =
                JsonMapper.shared().readValue(request.getInputStream(), EmailLoginRequest.class);
        String email = obtainEmail(emailLoginRequest);
        email = (email != null) ? email.trim() : "";
        String password = obtainPassword(emailLoginRequest);
        password = (password != null) ? password : "";

        // 封装成Spring Security需要的对象
        EmailAuthenticationToken authentication = new EmailAuthenticationToken(email, password);
        // 开始登录认证。SpringSecurity会利用 Authentication对象去寻找 AuthenticationProvider进行登录认证
        return getAuthenticationManager().authenticate(authentication);
    }

    @Nullable protected String obtainPassword(EmailLoginRequest emailLoginRequest) {
        return emailLoginRequest.password();
    }

    @Nullable protected String obtainEmail(EmailLoginRequest emailLoginRequest) {
        return emailLoginRequest.email();
    }
}
