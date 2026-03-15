package com.spring.security.authentication.handler.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import lombok.*;
import org.jspecify.annotations.Nullable;
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
    private Set<GrantedAuthority> authorities;
    private boolean accountNonLocked;
    private boolean accountNonExpired;
    private boolean credentialsNonExpired;
    private boolean enabled;
    private String mfaSecret;
    private Boolean mfaEnabled;

    public UserLoginInfo(
            String sessionId,
            Long id,
            String username,
            @Nullable String password,
            String phone,
            String email,
            boolean accountNonLocked,
            boolean accountNonExpired,
            boolean credentialsNonExpired,
            boolean enabled,
            String mfaSecret,
            Boolean mfaEnabled,
            Collection<? extends GrantedAuthority> authorities) {
        this.sessionId = sessionId;
        this.id = id;
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.email = email;
        this.accountNonLocked = accountNonLocked;
        this.accountNonExpired = accountNonExpired;
        this.credentialsNonExpired = credentialsNonExpired;
        this.enabled = enabled;
        this.mfaSecret = mfaSecret;
        this.mfaEnabled = mfaEnabled;
        this.authorities = Collections.unmodifiableSet(sortAuthorities(authorities));
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
