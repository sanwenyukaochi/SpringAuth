// package com.spring.security.authentication.handler.auth.def;
//
// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import java.io.IOException;
// import org.jspecify.annotations.NonNull;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.security.authentication.TestingAuthenticationToken;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.web.filter.OncePerRequestFilter;
//
// public class DefaultApiAuthenticationFilter extends OncePerRequestFilter {
//
//    @Value("${spring.security.user.name}")
//    private String username;
//
//    @Value("${spring.security.user.password}")
//    private String password;
//
//    @Override
//    protected void doFilterInternal(
//            @NonNull HttpServletRequest request, @NonNull HttpServletResponse response, FilterChain filterChain)
//            throws ServletException, IOException {
//        IO.println("这里是默认过滤链...");
//        // 随便给个默认身份
//        Authentication authentication = new TestingAuthenticationToken(username, password, "");
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//        // 放行
//        filterChain.doFilter(request, response);
//    }
// }
