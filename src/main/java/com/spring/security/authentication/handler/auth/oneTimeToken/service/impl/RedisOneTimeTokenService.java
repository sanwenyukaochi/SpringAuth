package com.spring.security.authentication.handler.auth.oneTimeToken.service.impl;

import com.spring.security.authentication.handler.auth.oneTimeToken.OneTimeTokenAuthenticationToken;
import com.spring.security.authentication.handler.auth.oneTimeToken.dto.RedisOneTimeToken;
import com.spring.security.authentication.handler.auth.oneTimeToken.service.OneTimeTokenService;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.redisson.api.RedissonClient;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.security.authentication.ott.DefaultOneTimeToken;
import org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
@RequiredArgsConstructor
public class RedisOneTimeTokenService implements OneTimeTokenService {

    private static final String KEY_PATTERN = "auth:one-time-token:%s";
    private final RedissonClient redissonClient;
    private Clock clock = Clock.systemUTC();

    @Override
    public @NullMarked OneTimeToken generate(GenerateOneTimeTokenRequest request) {
        String token = UUID.randomUUID().toString();
        Instant expiresAt = this.clock.instant().plus(request.getExpiresIn());
        OneTimeToken ott = new DefaultOneTimeToken(token, request.getUsername(), expiresAt);
        redissonClient
                .getBucket(KEY_PATTERN.formatted(token), new TypedJsonJacksonCodec(RedisOneTimeToken.class))
                .set(
                        new RedisOneTimeToken(
                                ott.getTokenValue(),
                                ott.getUsername(),
                                ott.getExpiresAt().getEpochSecond()),
                        request.getExpiresIn());
        return ott;
    }

    @Override
    public @Nullable OneTimeToken consume(OneTimeTokenAuthenticationToken authenticationToken) {
        String token = authenticationToken.getToken();
        RedisOneTimeToken redisOneTimeToken = (RedisOneTimeToken) redissonClient
                .getBucket(KEY_PATTERN.formatted(token), new TypedJsonJacksonCodec(RedisOneTimeToken.class))
                .getAndDelete();
        if (redisOneTimeToken == null) {
            return null;
        }
        OneTimeToken ott = new DefaultOneTimeToken(
                redisOneTimeToken.tokenValue(),
                redisOneTimeToken.username(),
                Instant.ofEpochSecond(redisOneTimeToken.expiresAt()));
        if (isExpired(ott)) {
            return null;
        }
        return ott;
    }

    public void setClock(Clock clock) {
        Assert.notNull(clock, "clock cannot be null");
        this.clock = clock;
    }

    private boolean isExpired(OneTimeToken ott) {
        return this.clock.instant().isAfter(ott.getExpiresAt());
    }
}
