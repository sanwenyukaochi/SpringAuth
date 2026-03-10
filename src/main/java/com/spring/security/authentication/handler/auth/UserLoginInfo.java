package com.spring.security.authentication.handler.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import lombok.*;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

/**
 * 用户信息登陆后的信息，会序列化到Jwt的payload
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginInfo implements UserDetails {

    private String sessionId; // 会话id，全局唯一
    private Long id;
    private String username;

    @JsonIgnore
    private String password;

    private String phone;
    private String email;
    private Boolean accountNonLocked;
    private Boolean accountNonExpired;
    private Boolean credentialsNonExpired;
    private Boolean enabled;
    private String twoFactorSecret;
    private Boolean twoFactorEnabled;

    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        // TODO 权限
        return List.of();
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    private static SortedSet<GrantedAuthority> sortAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Assert.notNull(authorities, "Cannot pass a null GrantedAuthority collection");
        SortedSet<GrantedAuthority> sortedAuthorities = new TreeSet<>(new AuthorityComparator());
        for (GrantedAuthority grantedAuthority : authorities) {
            Assert.notNull(grantedAuthority, "GrantedAuthority list cannot contain any null elements");
            sortedAuthorities.add(grantedAuthority);
        }
        return sortedAuthorities;
    }

    private static class AuthorityComparator implements Comparator<GrantedAuthority>, Serializable {

        @Serial
        private static final long serialVersionUID = 620L;

        @Override
        public int compare(GrantedAuthority g1, GrantedAuthority g2) {
            if (g2.getAuthority() == null) {
                return -1;
            }
            if (g1.getAuthority() == null) {
                return 1;
            }
            return g1.getAuthority().compareTo(g2.getAuthority());
        }
    }
}
