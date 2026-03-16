package org.oauth.client.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.oauth.client.authentication.github.GitHubOAuth2AuthorizationRedirectFilter;
import org.oauth.client.authentication.github.authentication.GitHubOAuth2AuthorizationCodeAuthenticationProvider;
import org.oauth.client.authentication.github.login.GitHubOAuth2LoginAuthenticationFilter;
import org.oauth.client.authentication.github.login.GitHubOAuth2LoginAuthenticationProvider;
import org.oauth.client.authentication.github.repository.GitHubHttpSessionOAuth2AuthorizationRequestRepository;
import org.oauth.client.handler.GitHubLoginFailHandler;
import org.oauth.client.handler.GitHubLoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.NullRequestCache;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final GitHubHttpSessionOAuth2AuthorizationRequestRepository
            gitHubHttpSessionOAuth2AuthorizationRequestRepository;

    private void commonHttpSetting(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.formLogin(AbstractHttpConfigurer::disable);
        httpSecurity.httpBasic(AbstractHttpConfigurer::disable);
        httpSecurity.logout(AbstractHttpConfigurer::disable);
        httpSecurity.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        httpSecurity.csrf(AbstractHttpConfigurer::disable);
        httpSecurity.cors(AbstractHttpConfigurer::disable);
        httpSecurity.requestCache(cache -> cache.requestCache(new NullRequestCache()));
        httpSecurity.rememberMe(AbstractHttpConfigurer::disable);
    }

    @Bean
    @Order(1)
    public SecurityFilterChain loginFilterChain(
            HttpSecurity httpSecurity,
            GitHubLoginSuccessHandler gitHubLoginSuccessHandler,
            GitHubLoginFailHandler gitHubLoginFailHandler,
            GitHubOAuth2AuthorizationCodeAuthenticationProvider gitHubOAuth2AuthorizationCodeAuthenticationProvider,
            GitHubOAuth2LoginAuthenticationProvider gitHubOAuth2LoginAuthenticationProvider)
            throws Exception {
        commonHttpSetting(httpSecurity);
        httpSecurity.securityMatcher("/api/login/**")
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());

        GitHubOAuth2AuthorizationRedirectFilter gitHubOAuth2AuthorizationRedirectFilter =
                new GitHubOAuth2AuthorizationRedirectFilter(
                        clientRegistrationRepository, gitHubHttpSessionOAuth2AuthorizationRequestRepository);
        httpSecurity.addFilterBefore(
                gitHubOAuth2AuthorizationRedirectFilter, UsernamePasswordAuthenticationFilter.class);

        GitHubOAuth2LoginAuthenticationFilter gitHubOAuth2LoginAuthenticationFilter =
                new GitHubOAuth2LoginAuthenticationFilter(
                        clientRegistrationRepository,
                        gitHubHttpSessionOAuth2AuthorizationRequestRepository,
                        new ProviderManager(List.of(
                                gitHubOAuth2LoginAuthenticationProvider,
                                gitHubOAuth2AuthorizationCodeAuthenticationProvider)),
                        gitHubLoginSuccessHandler,
                        gitHubLoginFailHandler);
        httpSecurity.addFilterBefore(gitHubOAuth2LoginAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain appFilterChain(HttpSecurity httpSecurity) throws Exception {
        commonHttpSetting(httpSecurity);
        httpSecurity.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
        return httpSecurity.build();
    }
}
