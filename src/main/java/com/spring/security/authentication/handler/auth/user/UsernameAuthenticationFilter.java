package com.spring.security.authentication.handler.auth.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import tools.jackson.databind.json.JsonMapper;

/**
 * 用户名密码登录
 * AbstractAuthenticationProcessingFilter 的实现类要做的工作：
 * 1. 从HttpServletRequest提取授权凭证。假设用户使用 用户名/密码 登录，就需要在这里提取username和password。
 * 然后，把提取到的授权凭证封装到的Authentication对象，并且authentication.isAuthenticated()一定返回false
 * 2. 将Authentication对象传给AuthenticationManager进行实际的授权操作
 */
@Slf4j
@Setter
public class UsernameAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final RequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER =
            PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/api/login/application/username");

    private boolean postOnly = true;

    private final JsonMapper jsonMapper;
    private final MessageSource messageSource;

    public UsernameAuthenticationFilter(
            AuthenticationManager authenticationManager,
            AuthenticationSuccessHandler authenticationSuccessHandler,
            AuthenticationFailureHandler authenticationFailureHandler,
            JsonMapper jsonMapper,
            MessageSource messageSource) {
        super(DEFAULT_ANT_PATH_REQUEST_MATCHER, authenticationManager);
        this.jsonMapper = jsonMapper;
        this.messageSource = messageSource;
        setAuthenticationSuccessHandler(authenticationSuccessHandler);
        setAuthenticationFailureHandler(authenticationFailureHandler);
    }

    @Override
    public Authentication attemptAuthentication(
            @NonNull HttpServletRequest request, @NonNull HttpServletResponse response)
            throws AuthenticationException, IOException {
        log.debug("use UsernameAuthenticationFilter");
        if (this.postOnly && !request.getMethod().equals(HttpMethod.POST.name())) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            jsonMapper.writeValue(
                    response.getOutputStream(),
                    ProblemDetail.forStatusAndDetail(
                            HttpStatus.UNAUTHORIZED,
                            messageSource.getMessage(
                                    "error.auth.method_not_supported",
                                    new Object[] {request.getMethod()},
                                    LocaleContextHolder.getLocale())));
            return null;
        }
        // 提取请求数据
        UsernameLoginRequest usernameLoginRequest =
                jsonMapper.readValue(request.getInputStream(), UsernameLoginRequest.class);
        String username = obtainUsername(usernameLoginRequest);
        String password = obtainPassword(usernameLoginRequest);

        // 封装成Spring Security需要的对象
        UsernameAuthenticationToken authRequest = UsernameAuthenticationToken.unauthenticated(username, password);
        // 开始登录认证。SpringSecurity会利用 Authentication对象去寻找 AuthenticationProvider进行登录认证
        return getAuthenticationManager().authenticate(authRequest);
    }

    @Nullable protected String obtainPassword(UsernameLoginRequest usernameLoginRequest) {
        return usernameLoginRequest.password();
    }

    @Nullable protected String obtainUsername(UsernameLoginRequest usernameLoginRequest) {
        return usernameLoginRequest.username();
    }
}
