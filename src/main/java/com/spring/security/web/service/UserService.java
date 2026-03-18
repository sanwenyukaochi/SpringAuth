package com.spring.security.web.service;

import com.spring.security.authentication.handler.auth.UserLoginInfo;
import com.spring.security.domain.model.entity.Role;
import com.spring.security.domain.model.entity.User;
import com.spring.security.domain.model.entity.UserRole;
import com.spring.security.domain.repository.UserRepository;
import com.spring.security.domain.repository.UserRoleRepository;
import com.spring.security.web.enums.BaseCode;
import com.spring.security.web.exception.BaseException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new BaseException(BaseCode.USER_NOT_FOUND));
    }

    public User getUserByPhone(String phone) {
        return userRepository.findByPhone(phone).orElseThrow(() -> new BaseException(BaseCode.USER_PHONE_NOT_FOUND));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new BaseException(BaseCode.USER_EMAIL_NOT_FOUND));
    }

    public User findById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new BaseException(BaseCode.USER_NOT_FOUND));
    }

    public UserLoginInfo loadUserByUsername(String username) {
        User loadedUser = getUserByUsername(username);
        Collection<GrantedAuthority> authorities = userRoleRepository.findByUser(loadedUser).stream()
                .map(UserRole::getRole)
                .map(Role::getCode)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toCollection(LinkedHashSet::new));
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

    public UserLoginInfo loadUserByEmail(String email) {
        User loadedUser = getUserByEmail(email);
        Collection<GrantedAuthority> authorities = userRoleRepository.findByUser(loadedUser).stream()
                .map(UserRole::getRole)
                .map(Role::getCode)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toCollection(LinkedHashSet::new));
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
}
