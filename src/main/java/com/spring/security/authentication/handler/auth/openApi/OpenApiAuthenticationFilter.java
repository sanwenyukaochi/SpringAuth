// package com.spring.security.authentication.handler.auth.openApi;
//
// import com.spring.security.common.web.exception.BaseException;
// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import java.io.IOException;
// import lombok.extern.slf4j.Slf4j;
// import org.jspecify.annotations.NonNull;
// import org.springframework.http.HttpStatus;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.util.StringUtils;
// import org.springframework.web.filter.OncePerRequestFilter;
//
// @Slf4j
// public class OpenApiAuthenticationFilter extends OncePerRequestFilter {
//
//    public OpenApiAuthenticationFilter() {}
//
//    @Override
//    protected void doFilterInternal(
//            @NonNull HttpServletRequest request,
//            @NonNull HttpServletResponse response,
//            @NonNull FilterChain filterChain)
//            throws ServletException, IOException {
//        log.debug("Use OpenApi2AuthenticationFilter...");
//
//        String appId = request.getHeader("x-app-id");
//        if (!StringUtils.hasText(appId)) {
//            throw new BaseException("miss.appId", "x-app-id token is missing!", HttpStatus.BAD_REQUEST);
//        }
//
//        // 认证开始前，按SpringSecurity设计，要将Authentication设置到SecurityContext里面去。
//        log.info("appId认证通过...");
//
//        OpenApiAuthenticationToken authentication = new OpenApiAuthenticationToken();
//
//        OpenApiLoginInfo userLoginInfo = new OpenApiLoginInfo();
//        userLoginInfo.setAppId(appId);
//        userLoginInfo.setMerchantName("三方系统商户名称");
//
//        authentication.setAuthenticated(true); // 设置true，认证通过。
//        authentication.setCurrentUser(userLoginInfo);
//        SecurityContextHolder.getContext().setAuthentication(authentication); // 一定要设置到ThreadLocal
//
//        // 放行
//        filterChain.doFilter(request, response);
//    }
// }
