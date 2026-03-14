package com.spring.security.common.web.service;

import java.security.SecureRandom;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class RedisVerificationCodeService {

    private static final String KEY_PATTERN = "verification:code:%s:%s:%s";
    private static final String DIGITS = "0123456789";
    private static final int CODE_LENGTH = 6;

    private final RedissonClient redissonClient;
    private final SecureRandom secureRandom = new SecureRandom();

    public String generate(
            String subject, VerificationChannel channel, VerificationPurpose purpose, Duration expiresIn) {
        Assert.hasText(subject, "subject cannot be empty");
        Assert.notNull(channel, "channel cannot be null");
        Assert.notNull(purpose, "purpose cannot be null");
        Assert.notNull(expiresIn, "expiresIn cannot be null");
        Assert.isTrue(!expiresIn.isNegative() && !expiresIn.isZero(), "expiresIn must be positive");
        String code = generateCode();
        this.redissonClient
                .getBucket(KEY_PATTERN.formatted(
                        channel.name().toLowerCase(),
                        purpose.name().toLowerCase(),
                        subject.trim().toLowerCase()))
                .set(code, expiresIn);
        return code;
    }

    public boolean verifyAndConsume(
            String subject, VerificationChannel channel, VerificationPurpose purpose, String code) {
        Assert.hasText(subject, "subject cannot be empty");
        Assert.notNull(channel, "channel cannot be null");
        Assert.notNull(purpose, "purpose cannot be null");
        if (!StringUtils.hasText(code)) {
            return false;
        }
        String storedCode = (String) redissonClient
                .getBucket(KEY_PATTERN.formatted(
                        channel.name().toLowerCase(),
                        purpose.name().toLowerCase(),
                        subject.trim().toLowerCase()))
                .getAndDelete();
        return code.equals(storedCode);
    }

    public void delete(String subject, VerificationChannel channel, VerificationPurpose purpose) {
        Assert.hasText(subject, "subject cannot be empty");
        Assert.notNull(channel, "channel cannot be null");
        Assert.notNull(purpose, "purpose cannot be null");
        redissonClient
                .getBucket(KEY_PATTERN.formatted(
                        channel.name().toLowerCase(),
                        purpose.name().toLowerCase(),
                        subject.trim().toLowerCase()))
                .delete();
    }

    public String get(String subject, VerificationChannel channel, VerificationPurpose purpose) {
        Assert.hasText(subject, "subject cannot be empty");
        Assert.notNull(channel, "channel cannot be null");
        Assert.notNull(purpose, "purpose cannot be null");
        return (String) redissonClient
                .getBucket(KEY_PATTERN.formatted(
                        channel.name().toLowerCase(),
                        purpose.name().toLowerCase(),
                        subject.trim().toLowerCase()))
                .get();
    }

    private String generateCode() {
        StringBuilder builder = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = secureRandom.nextInt(DIGITS.length());
            builder.append(DIGITS.charAt(index));
        }
        return builder.toString();
    }

    public enum VerificationChannel {
        SMS,
        EMAIL
    }

    public enum VerificationPurpose {
        LOGIN,
        BIND,
        RESET_PASSWORD
    }
}
