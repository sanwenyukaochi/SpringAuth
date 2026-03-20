package com.spring.security.authentication.handler.auth.jwt;

import com.spring.security.authentication.handler.auth.jwt.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT令牌认证过滤器
 * 职责：
 * 1. 从HTTP请求中提取JWT令牌
 * 2. 创建未认证的JwtTokenAuthentication对象
 * 3. 委托给AuthenticationManager进行实际认证
 * 4. 将认证结果设置到SecurityContext中
 * 设计理念：
 * - 遵循Spring Security的标准认证流程
 * - Filter只负责提取凭证，不做验证逻辑
 * - 认证逻辑交给AuthenticationProvider处理
 * - 异常处理交给Spring Security的异常处理机制
 */
@Slf4j
@Setter
@RequiredArgsConstructor
public class JwtTokenAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final MessageSource messageSource;
    private boolean postOnly = true;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        log.debug("use JwtTokenAuthenticationFilter");
        if (this.postOnly && !request.getMethod().equals(HttpMethod.POST.name())) {
            throw new AuthenticationServiceException(messageSource.getMessage(
                    "error.auth.method_not_supported",
                    new Object[] {request.getMethod()},
                    LocaleContextHolder.getLocale()));
        }
        // 提取请求数据
        String jwtToken = jwtService.getJwtFromHeader(request);
        jwtToken = obtainJwtToken(jwtToken);
        // 如果没有JWT令牌，直接放行(可能是公开接口或其他认证方式)
        if (!StringUtils.hasText(jwtToken)) {
            log.debug("未找到JWT令牌，跳过JWT认证");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 创建未认证的Authentication对象
            JwtTokenAuthenticationToken authRequest = JwtTokenAuthenticationToken.unauthenticated(jwtToken);
            authRequest.setDetails(new WebAuthenticationDetails(request));
            // 委托给AuthenticationManager进行认证
            Authentication authenticatedToken = authenticationManager.authenticate(authRequest);

            // 认证成功，将结果设置到SecurityContext中
            SecurityContextHolder.getContext().setAuthentication(authenticatedToken);
            log.debug("JWT认证成功，用户已设置到SecurityContext");

        } catch (AuthenticationException e) {
            // 认证失败，清空SecurityContext
            SecurityContextHolder.clearContext();
            log.debug("JWT认证失败: {}", e.getMessage());
            // 注意：这里不直接抛出异常，而是让Spring Security的异常处理机制处理
            // 异常会被ExceptionTranslationFilter捕获，然后调用AuthenticationEntryPoint
            throw e;
        }

        // 继续过滤器链
        filterChain.doFilter(request, response);
    }

    @Nullable protected String obtainJwtToken(String jwtToken) {
        return jwtToken;
    }
}
