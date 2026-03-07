package com.spring.security.authentication.handler.auth.github;

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
public class GitHubAuthenticationToken extends AbstractAuthenticationToken {

    private String code;
    private UserLoginInfo currentUser;

    public GitHubAuthenticationToken(String code) {
        this.code = code;
        super(List.of());
        super.setAuthenticated(false);
    }

    public GitHubAuthenticationToken(UserLoginInfo currentUser, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.currentUser = currentUser;
        super.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return isAuthenticated() ? null : code;
    }

    @Override
    public Object getPrincipal() {
        return isAuthenticated() ? currentUser : code;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        Assert.isTrue(!isAuthenticated, "无法将此令牌设置为受信任令牌 - 请改用接受 GrantedAuthority 列表的构造函数。");
        super.setAuthenticated(false);
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.code = null;
    }
}
