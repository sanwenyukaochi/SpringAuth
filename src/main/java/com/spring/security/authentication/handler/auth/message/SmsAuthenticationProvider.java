package com.spring.security.authentication.handler.auth.message;

import com.spring.security.authentication.handler.auth.UserLoginInfo;
import com.spring.security.authentication.handler.authorization.Authority;
import com.spring.security.web.service.RedisVerificationCodeService;
import com.spring.security.web.service.RedisVerificationCodeService.VerificationChannel;
import com.spring.security.web.service.RedisVerificationCodeService.VerificationPurpose;
import com.spring.security.web.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.authority.FactorGrantedAuthority;
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
    private final RedisVerificationCodeService redisVerificationCodeService;
    private final UserService userService;
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
        UserLoginInfo userLoginInfo = userService.loadUserByPhone(phone);
        userLoginInfo.getAuthorities().add(FactorGrantedAuthority.fromAuthority(AUTHORITY));
        log.debug("用户信息查询成功，用户: {}", userLoginInfo.getUsername());
        // 验证用户信息
        String inputCode = smsAuthenticationToken.getSmsCode();
        if (redisVerificationCodeService.verifyAndConsume(
                phone, VerificationChannel.SMS, VerificationPurpose.LOGIN, inputCode)) {
            log.debug("身份验证失败，因为短信验证码无效、已使用或已过期");
            throw new BadCredentialsException(
                    this.messages.getMessage("smsAuthenticationProvider.badCredentials", "Bad credentials"));
        }
        // 构造成功结果
        // 认证通过，使用 Authenticated 为 true 的构造函数
        SmsAuthenticationToken result = SmsAuthenticationToken.authenticated(userLoginInfo, List.of());
        // 必须转化成Map
        result.setDetails(authentication);
        log.debug("手机号认证成功，用户: {}", userLoginInfo.getUsername());
        return result;
    }

    @Override
    public boolean supports(@NonNull Class<?> authentication) {
        return SmsAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
