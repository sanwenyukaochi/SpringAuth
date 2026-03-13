package com.spring.security.authentication.handler.auth.message;

import com.spring.security.authentication.handler.auth.UserLoginInfo;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

@Setter
@Getter
public class SmsAuthenticationToken extends AbstractAuthenticationToken {

    private String phone;
    private String smsCode;
    private UserLoginInfo currentUser;

    private SmsAuthenticationToken(String phone, String smsCode) {
        this.phone = phone;
        this.smsCode = smsCode;
        super(List.of());
        super.setAuthenticated(false);
    }

    private SmsAuthenticationToken(UserLoginInfo currentUser, Collection<? extends GrantedAuthority> authorities) {
        this.currentUser = currentUser;
        super(authorities);
        super.setAuthenticated(true);
    }

    public static SmsAuthenticationToken unauthenticated(@Nullable String username, @Nullable String smsCode) {
        return new SmsAuthenticationToken(username, smsCode);
    }

    public static SmsAuthenticationToken authenticated(
            UserLoginInfo currentUser, Collection<? extends GrantedAuthority> authorities) {
        return new SmsAuthenticationToken(currentUser, authorities);
    }

    @Override
    public Object getCredentials() {
        return smsCode;
    }

    @Override
    public UserLoginInfo getPrincipal() {
        return currentUser;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        Assert.isTrue(!isAuthenticated, "无法将此令牌设置为受信任令牌 - 请改用接受 GrantedAuthority 列表的构造函数。");
        super.setAuthenticated(false);
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.smsCode = null;
    }
}
