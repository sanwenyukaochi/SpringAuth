package com.spring.security.authentication.handler.auth.email;

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
public class EmailAuthenticationToken extends AbstractAuthenticationToken {

    private String email;
    private String password;
    private UserLoginInfo currentUser;

    private EmailAuthenticationToken(String email, String password) {
        this.email = email;
        this.password = password;
        super(List.of());
        super.setAuthenticated(false);
    }

    private EmailAuthenticationToken(UserLoginInfo currentUser, Collection<? extends GrantedAuthority> authorities) {
        this.currentUser = currentUser;
        super(authorities);
        super.setAuthenticated(true);
    }

    public static EmailAuthenticationToken unauthenticated(@Nullable String email, @Nullable String password) {
        return new EmailAuthenticationToken(email, password);
    }

    public static EmailAuthenticationToken authenticated(
            UserLoginInfo currentUser, Collection<? extends GrantedAuthority> authorities) {
        return new EmailAuthenticationToken(currentUser, authorities);
    }

    @Override
    public Object getCredentials() {
        return password;
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
        this.password = null;
    }
}
