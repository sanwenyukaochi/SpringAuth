package com.spring.security.authentication.config;

import com.spring.security.authentication.handler.auth.LoginFailHandler;
import com.spring.security.authentication.handler.auth.LoginSuccessHandler;
import com.spring.security.authentication.handler.auth.def.DefaultApiAuthenticationFilter;
import com.spring.security.authentication.handler.auth.email.EmailAuthenticationFilter;
import com.spring.security.authentication.handler.auth.email.EmailAuthenticationProvider;
import com.spring.security.authentication.handler.auth.github.GitHubAuthorizationRedirectFilter;
import com.spring.security.authentication.handler.auth.github.HttpCookieGitHubAuthorizationRequestRepository;
import com.spring.security.authentication.handler.auth.github.authentication.GitHubAuthorizationCodeAuthenticationProvider;
import com.spring.security.authentication.handler.auth.github.login.GitHubLoginAuthenticationFilter;
import com.spring.security.authentication.handler.auth.github.login.GitHubLoginAuthenticationProvider;
import com.spring.security.authentication.handler.auth.jwt.JwtTokenAuthenticationFilter;
import com.spring.security.authentication.handler.auth.jwt.JwtTokenAuthenticationProvider;
import com.spring.security.authentication.handler.auth.jwt.service.JwtService;
import com.spring.security.authentication.handler.auth.message.SmsAuthenticationFilter;
import com.spring.security.authentication.handler.auth.message.SmsAuthenticationProvider;
import com.spring.security.authentication.handler.auth.user.UsernameAuthenticationFilter;
import com.spring.security.authentication.handler.auth.user.UsernameAuthenticationProvider;
import com.spring.security.authentication.handler.exception.CustomAuthenticationExceptionHandler;
import com.spring.security.authentication.handler.exception.CustomAuthorizationExceptionHandler;
import com.spring.security.authentication.handler.exception.CustomSecurityExceptionHandler;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.savedrequest.NullRequestCache;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class CustomWebSecurityConfig {

    private final CustomAuthenticationExceptionHandler authenticationExceptionHandler;
    private final CustomAuthorizationExceptionHandler authorizationExceptionHandler;
    private final CustomSecurityExceptionHandler globalSpringSecurityExceptionHandler;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final HttpCookieGitHubAuthorizationRequestRepository httpCookieGitHubAuthorizationRequestRepository;
    /**
     * 禁用不必要的默认filter，处理异常响应内容
     */
    private void commonHttpSetting(HttpSecurity httpSecurity) {
        // 禁用SpringSecurity默认filter。这些filter都是非前后端分离项目的产物，用不上.
        // properties配置文件将日志设置DEBUG模式，就能看到加载了哪些filter
        // logging.level.org.springframework.security=DEBUG
        // 不disable。会默认创建默认filter

        // 前端段分离不需要---禁用默认登录页
        httpSecurity.formLogin(AbstractHttpConfigurer::disable);
        // 前端段分离不需要---禁用明文验证
        httpSecurity.httpBasic(AbstractHttpConfigurer::disable);
        // 前端段分离不需要---禁用退出页
        httpSecurity.logout(AbstractHttpConfigurer::disable);
        // 前后端分离不需要---因为是无状态的
        httpSecurity.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        // 前端段分离不需要---csrf攻击
        httpSecurity.csrf(AbstractHttpConfigurer::disable);
        // 跨域访问权限，如果需要可以关闭后自己配置跨域访问
        httpSecurity.cors(AbstractHttpConfigurer::disable);
        // requestCache用于重定向，前后端分析项目无需重定向，requestCache也用不上
        httpSecurity.requestCache(cache -> cache.requestCache(new NullRequestCache()));
        // 前后端分离不需要---记住我
        httpSecurity.rememberMe(AbstractHttpConfigurer::disable);
        // 无需给用户一个匿名身份
        httpSecurity.anonymous(AbstractHttpConfigurer::disable);
        // 处理 SpringSecurity 异常响应结果。响应数据的结构，改成业务统一的JSON结构。不要框架默认的响应结构
        httpSecurity.exceptionHandling(exceptionHandling -> {
            // 认证失败异常
            exceptionHandling.authenticationEntryPoint(authenticationExceptionHandler);
            // 鉴权失败异常
            exceptionHandling.accessDeniedHandler(authorizationExceptionHandler);
        });
        // 其他未知异常. 尽量提前加载。
        httpSecurity.addFilterBefore(globalSpringSecurityExceptionHandler, SecurityContextHolderFilter.class);
    }

    /**
     * 密码加密使用的编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 登录api
     */
    @Bean
    @Order(1)
    public SecurityFilterChain loginFilterChain(
            HttpSecurity httpSecurity,
            LoginSuccessHandler loginSuccessHandler,
            LoginFailHandler loginFailHandler,
            UsernameAuthenticationProvider usernameAuthenticationProvider,
            SmsAuthenticationProvider smsAuthenticationProvider,
            EmailAuthenticationProvider emailAuthenticationProvider,
            GitHubAuthorizationCodeAuthenticationProvider gitHubAuthorizationCodeAuthenticationProvider,
            GitHubLoginAuthenticationProvider gitHubLoginAuthenticationProvider) {
        commonHttpSetting(httpSecurity);
        // 使用securityMatcher限定当前配置作用的路径
        httpSecurity
                .securityMatcher("/api/login/**")
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated());

        // 用户名密码认证过滤器
        UsernameAuthenticationFilter usernameAuthenticationFilter = new UsernameAuthenticationFilter(
                new ProviderManager(List.of(usernameAuthenticationProvider)), loginSuccessHandler, loginFailHandler);
        httpSecurity.addFilterBefore(usernameAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // 短信验证码认证过滤器
        SmsAuthenticationFilter smsAuthenticationFilter = new SmsAuthenticationFilter(
                new ProviderManager(List.of(smsAuthenticationProvider)), loginSuccessHandler, loginFailHandler);
        httpSecurity.addFilterBefore(smsAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // 邮箱密码认证过滤器
        EmailAuthenticationFilter emailAuthenticationFilter = new EmailAuthenticationFilter(
                new ProviderManager(List.of(emailAuthenticationProvider)), loginSuccessHandler, loginFailHandler);
        httpSecurity.addFilterBefore(emailAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // GitHub OAuth2 授权请求过滤器 - 负责跳转到GitHub登录页
        GitHubAuthorizationRedirectFilter gitHubAuthorizationRedirectFilter = new GitHubAuthorizationRedirectFilter(
                clientRegistrationRepository, httpCookieGitHubAuthorizationRequestRepository);
        httpSecurity.addFilterBefore(gitHubAuthorizationRedirectFilter, UsernamePasswordAuthenticationFilter.class);

        // GitHub OAuth2 回调认证过滤器 - 处理GitHub回调并完成登录
        GitHubLoginAuthenticationFilter gitHubLoginAuthenticationFilter = new GitHubLoginAuthenticationFilter(
                clientRegistrationRepository,
                httpCookieGitHubAuthorizationRequestRepository,
                new ProviderManager(
                        List.of(gitHubLoginAuthenticationProvider, gitHubAuthorizationCodeAuthenticationProvider)),
                loginSuccessHandler,
                loginFailHandler);
        httpSecurity.addFilterBefore(gitHubLoginAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    /**
     * Open自定义api
     */
    @Bean
    @Order(2)
    public SecurityFilterChain openApiFilterChain(HttpSecurity httpSecurity) {
        commonHttpSetting(httpSecurity);
        // 不使用securityMatcher限定当前配置作用的路径。所有没有匹配上指定SecurityFilterChain的请求，都走这里鉴权
        httpSecurity
                .securityMatcher("/api/open-api/**")
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated());

        //        OpenApiAuthenticationFilter openApiFilter = new OpenApiAuthenticationFilter();
        // 加一个登录方式。用户名、密码登录
        //        httpSecurity.addFilterBefore(openApiFilter, UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }

    /**
     * 不鉴权的api
     */
    @Bean
    @Order(3)
    public SecurityFilterChain publicApiFilterChain(HttpSecurity httpSecurity) {
        commonHttpSetting(httpSecurity);
        // 使用securityMatcher限定当前配置作用的路径
        httpSecurity
                .securityMatcher(
                        "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/swagger-resources/**", "/webjars/**")
                .securityMatcher("/api/public-api/**")
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
        return httpSecurity.build();
    }

    /**
     * 需要认证api
     */
    @Bean
    @Order(4)
    public SecurityFilterChain JwtTokenApiFilterChain(
            HttpSecurity http, JwtService jwtService, JwtTokenAuthenticationProvider jwtTokenAuthenticationProvider) {
        commonHttpSetting(http);
        // 使用securityMatcher限定当前配置作用的路径
        http.securityMatcher("/api/**")
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated());

        // 创建JWT认证过滤器，使用AuthenticationManager
        JwtTokenAuthenticationFilter jwtFilter = new JwtTokenAuthenticationFilter(
                jwtService, new ProviderManager(List.of(jwtTokenAuthenticationProvider)));
        jwtFilter.setPostOnly(false);
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * 其余路径，走这个默认过滤链
     */
    @Bean
    @Order(Integer.MAX_VALUE) // 这个过滤链最后加载
    @ConditionalOnProperty(
            name = "spring.security.default-api-filter-chain",
            havingValue = "true",
            matchIfMissing = false)
    public SecurityFilterChain defaultApiFilterChain(HttpSecurity http) {
        commonHttpSetting(http);
        // 不用securityMatcher表示缺省值，匹配不上其他过滤链时，都走这个过滤链
        http.authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated());
        http.addFilterBefore(new DefaultApiAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
