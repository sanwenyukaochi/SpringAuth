package com.spring.security.authentication.handler.auth.oneTimeToken;

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
public class OneTimeTokenAuthenticationToken extends AbstractAuthenticationToken {

    private String token; // 前端传过来
    private UserLoginInfo currentUser; // 认证成功后，后台从数据库获取信息

    private OneTimeTokenAuthenticationToken(String token) {
        this.token = token;
        super(List.of());
        super.setAuthenticated(false);
    }

    private OneTimeTokenAuthenticationToken(
            UserLoginInfo currentUser, Collection<? extends GrantedAuthority> authorities) {
        this.currentUser = currentUser;
        super(authorities);
        super.setAuthenticated(true);
    }

    public static OneTimeTokenAuthenticationToken unauthenticated(@Nullable String token) {
        return new OneTimeTokenAuthenticationToken(token);
    }

    public static OneTimeTokenAuthenticationToken authenticated(
            UserLoginInfo currentUser, Collection<? extends GrantedAuthority> authorities) {
        return new OneTimeTokenAuthenticationToken(currentUser, authorities);
    }

    @Override
    public @Nullable Object getCredentials() {
        return token;
    }

    @Override
    public @Nullable UserLoginInfo getPrincipal() {
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
        this.token = null;
    }
}
