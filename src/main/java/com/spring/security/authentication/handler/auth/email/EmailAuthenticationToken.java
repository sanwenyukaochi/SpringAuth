package com.spring.security.authentication.handler.auth.email;

import com.spring.security.authentication.handler.auth.UserLoginInfo;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

@Setter
@Getter
public class EmailAuthenticationToken extends AbstractAuthenticationToken {

    private String email;
    private String password;
    private UserLoginInfo currentUser;

    public EmailAuthenticationToken(String email, String password) {
        this.email = email;
        this.password = password;
        super(List.of());
        super.setAuthenticated(false);
    }

    public EmailAuthenticationToken(UserLoginInfo currentUser, Collection<? extends GrantedAuthority> authorities) {
        this.currentUser = currentUser;
        super(authorities);
        super.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return isAuthenticated() ? null : password;
    }

    @Override
    public UserLoginInfo getPrincipal() {
        return isAuthenticated() ? currentUser : new UserLoginInfo();
    }

    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        Assert.isTrue(!isAuthenticated, "无法将此令牌设置为受信任令牌 - 请改用接受 GrantedAuthority 列表的构造函数。");
        super.setAuthenticated(false);
    }

    public void eraseCredentials() {
        super.eraseCredentials();
        this.password = null;
    }
}
