package com.spring.security.web.config;

import com.spring.security.authentication.handler.auth.UserLoginInfo;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "dateTimeProvider")
public class SpringDataJpaConfig {

    @Bean
    public DateTimeProvider dateTimeProvider() {
        return () -> Optional.of(Instant.now().truncatedTo(ChronoUnit.MILLIS));
    }

    @Bean
    public AuditorAware<@NonNull Long> auditorProvider() {
        return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .filter(principal -> principal instanceof UserLoginInfo)
                .map(principal -> ((UserLoginInfo) principal).getId());
    }
}
