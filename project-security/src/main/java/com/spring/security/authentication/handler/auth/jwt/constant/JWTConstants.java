package com.spring.security.authentication.handler.auth.jwt.constant;

import java.util.concurrent.TimeUnit;

public final class JWTConstants {
    public static final String USER_INFO = "userInfo";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final Long TOKEN_EXPIRED_TIME = TimeUnit.MINUTES.toMillis(10);
    public static final Long REFRESH_TOKEN_EXPIRED_TIME = TimeUnit.DAYS.toMillis(30);
}
