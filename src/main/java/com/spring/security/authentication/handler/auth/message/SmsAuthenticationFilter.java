package com.spring.security.authentication.handler.auth.message;

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

@Slf4j
@Setter
public class SmsAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final RequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER =
            PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/api/login/application/sms");

    private boolean postOnly = true;

    private final JsonMapper jsonMapper;
    private final MessageSource messageSource;

    public SmsAuthenticationFilter(
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
        log.debug("user SmsCodeAuthenticationFilter");
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
        SmsLoginRequest smsLoginRequest = jsonMapper.readValue(request.getInputStream(), SmsLoginRequest.class);
        String phone = obtainPhone(smsLoginRequest);
        String captcha = obtainCaptcha(smsLoginRequest);

        // 封装成Spring Security需要的对象
        SmsAuthenticationToken authentication = SmsAuthenticationToken.unauthenticated(phone, captcha);
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
