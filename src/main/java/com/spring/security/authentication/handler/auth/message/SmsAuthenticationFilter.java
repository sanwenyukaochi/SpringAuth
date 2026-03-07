package com.spring.security.authentication.handler.auth.message;

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
public class SmsAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final RequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER =
            PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/api/login/application/sms");

    private boolean postOnly = true;

    public SmsAuthenticationFilter(
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
        log.debug("user SmsCodeAuthenticationFilter");
        if (this.postOnly && !request.getMethod().equals(HttpMethod.POST.name())) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }
        // 提取请求数据
        SmsLoginRequest smsLoginRequest =
                JsonMapper.shared().readValue(request.getInputStream(), SmsLoginRequest.class);
        String phone = obtainPhone(smsLoginRequest);
        phone = (phone != null) ? phone.trim() : "";
        String captcha = obtainCaptcha(smsLoginRequest);
        captcha = (captcha != null) ? captcha : "";

        // 封装成Spring Security需要的对象
        SmsAuthenticationToken authentication = new SmsAuthenticationToken(phone, captcha);
        // 提取参数阶段，authenticated一定是false
        return getAuthenticationManager().authenticate(authentication);
    }

    @Nullable protected String obtainPhone(SmsLoginRequest smsLoginRequest) {
        return smsLoginRequest.phone();
    }

    @Nullable protected String obtainCaptcha(SmsLoginRequest smsLoginRequest) {
        return smsLoginRequest.captcha();
    }
}
