package com.spring.security.web.service;

import com.spring.security.authentication.handler.auth.LoginResponse;
import com.spring.security.authentication.handler.auth.UserLoginInfo;
import com.spring.security.authentication.handler.auth.jwt.constant.JWTConstants;
import com.spring.security.authentication.handler.auth.jwt.dto.JwtTokenUserLoginInfo;
import com.spring.security.authentication.handler.auth.jwt.service.JwtService;
import com.spring.security.web.constant.RedisCache;
import com.spring.security.web.model.dto.GitHubOAuthLoginRequest;
import com.spring.security.web.model.entity.User;
import com.spring.security.web.model.entity.UserIdentity;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GitHubOAuthLoginService {

    private final UserIdentityService userIdentityService;
    private final UserService userService;
    private final JwtService jwtService;
    private final RedissonClient redissonClient;

    public LoginResponse loginOrPrepareBinding(GitHubOAuthLoginRequest request) {
        Map<String, Object> additionalInfo = new LinkedHashMap<>();
        additionalInfo.put("provider", UserIdentity.Provider.GITHUB.name());
        additionalInfo.put("providerUserId", request.providerUserId());
        additionalInfo.put("login", request.login());
        additionalInfo.put("name", request.name());
        additionalInfo.put("email", request.email());

        return userIdentityService
                .findByProviderUserIdAndProvider(request.providerUserId(), UserIdentity.Provider.GITHUB)
                .map(UserIdentity::getUserId)
                .map(userService::findById)
                .map(user -> issueLoginResponse(user, additionalInfo))
                .orElseGet(() -> new LoginResponse(
                        null,
                        null,
                        mergeAdditionalInfo(additionalInfo, Map.of("isNewUser", true))));
    }

    private LoginResponse issueLoginResponse(User user, Map<String, Object> additionalInfo) {
        UserLoginInfo currentUser = new UserLoginInfo(
                UUID.randomUUID().toString(),
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getPhone(),
                user.getEmail(),
                user.getAccountNonLocked(),
                user.getAccountNonExpired(),
                user.getCredentialsNonExpired(),
                user.getEnabled(),
                user.getMfaSecret(),
                user.getMfaEnabled(),
                Set.of());
        JwtTokenUserLoginInfo jwtTokenUserLoginInfo =
                new JwtTokenUserLoginInfo(currentUser.getSessionId(), currentUser.getUsername());
        String token = jwtService.generateTokenFromUsername(
                currentUser.getUsername(), jwtTokenUserLoginInfo, JWTConstants.TOKEN_EXPIRED_TIME);
        String refreshToken = jwtService.generateTokenFromUsername(
                currentUser.getUsername(), jwtTokenUserLoginInfo, JWTConstants.REFRESH_TOKEN_EXPIRED_TIME);
        redissonClient
                .getBucket(
                        RedisCache.USER_INFO.formatted(jwtTokenUserLoginInfo.username()),
                        new TypedJsonJacksonCodec(UserLoginInfo.class))
                .set(currentUser);
        return new LoginResponse(token, refreshToken, mergeAdditionalInfo(additionalInfo, Map.of("isNewUser", false)));
    }

    private Map<String, Object> mergeAdditionalInfo(Map<String, Object> base, Map<String, Object> overrides) {
        LinkedHashMap<String, Object> merged = new LinkedHashMap<>(base);
        merged.putAll(overrides);
        return merged;
    }
}
