package com.spring.security.authentication.handler.auth.message;

import com.spring.security.authentication.handler.auth.UserLoginInfo;
import com.spring.security.authentication.handler.authorization.Authority;
import com.spring.security.domain.model.entity.Role;
import com.spring.security.domain.model.entity.User;
import com.spring.security.domain.model.entity.UserRole;
import com.spring.security.domain.repository.UserRepository;
import com.spring.security.domain.repository.UserRoleRepository;
import com.spring.security.web.enums.BaseCode;
import com.spring.security.web.exception.BaseException;
import com.spring.security.web.service.RedisVerificationCodeService;
import com.spring.security.web.service.RedisVerificationCodeService.VerificationChannel;
import com.spring.security.web.service.RedisVerificationCodeService.VerificationPurpose;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 手机号验证码登录认证
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SmsAuthenticationProvider implements AuthenticationProvider {
    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
    private final UserRepository userRepository;
    private final RedisVerificationCodeService redisVerificationCodeService;
    private final UserRoleRepository userRoleRepository;
    private static final String AUTHORITY = Authority.SMS_AUTHORITY;

    @Override
    public Authentication authenticate(@NonNull Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(
                SmsAuthenticationToken.class,
                authentication,
                () -> this.messages.getMessage("SmsAuthenticationProvider.onlySupports", "仅支持手机号验证码身份验证提供程序"));
        SmsAuthenticationToken smsAuthenticationToken = (SmsAuthenticationToken) authentication;
        // 获取用户提交的手机号
        String phone =
                (smsAuthenticationToken.getPhone() == null ? "NONE_PROVIDED" : smsAuthenticationToken.getPhone());
        // 查询用户信息
        UserLoginInfo userLoginInfo = retrieveUser(phone, smsAuthenticationToken);
        // 验证用户信息
        additionalAuthenticationChecks(userLoginInfo, (SmsAuthenticationToken) authentication);
        // 构造成功结果
        return createSuccessAuthentication(smsAuthenticationToken, userLoginInfo);
    }

    @Override
    public boolean supports(@NonNull Class<?> authentication) {
        return SmsAuthenticationToken.class.isAssignableFrom(authentication);
    }

    protected Authentication createSuccessAuthentication(Authentication authentication, UserLoginInfo userLoginInfo) {
        // 认证通过，使用 Authenticated 为 true 的构造函数
        SmsAuthenticationToken result = SmsAuthenticationToken.authenticated(userLoginInfo, List.of());
        // 必须转化成Map
        result.setDetails(authentication);
        log.debug("手机号认证成功，用户: {}", userLoginInfo.getUsername());
        return result;
    }

    protected UserLoginInfo retrieveUser(String phone, SmsAuthenticationToken authentication)
            throws AuthenticationException {
        User loadedUser =
                userRepository.findByPhone(phone).orElseThrow(() -> new BaseException(BaseCode.USER_PHONE_NOT_FOUND));
        Collection<GrantedAuthority> authorities = userRoleRepository.findByUser(loadedUser).stream()
                .map(UserRole::getRole)
                .map(Role::getCode)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        authorities.add(FactorGrantedAuthority.fromAuthority(AUTHORITY));
        log.debug("用户信息查询成功，用户: {}", loadedUser.getUsername());
        return new UserLoginInfo(
                UUID.randomUUID().toString(),
                loadedUser.getId(),
                loadedUser.getUsername(),
                loadedUser.getPassword(),
                loadedUser.getPhone(),
                loadedUser.getEmail(),
                loadedUser.getAccountNonLocked(),
                loadedUser.getAccountNonExpired(),
                loadedUser.getCredentialsNonExpired(),
                loadedUser.getEnabled(),
                loadedUser.getMfaSecret(),
                loadedUser.getMfaEnabled(),
                authorities);
    }

    protected void additionalAuthenticationChecks(UserLoginInfo userLoginInfo, SmsAuthenticationToken authentication)
            throws AuthenticationException {
        String phone = authentication.getPhone();
        String inputCode = authentication.getSmsCode();
        if (redisVerificationCodeService.verifyAndConsume(
                phone, VerificationChannel.SMS, VerificationPurpose.LOGIN, inputCode)) {
            log.debug("身份验证失败，因为短信验证码无效、已使用或已过期");
            throw new BadCredentialsException(
                    this.messages.getMessage("smsAuthenticationProvider.badCredentials", "Bad credentials"));
        }
    }
}
