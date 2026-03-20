package com.spring.security.web.service;

import com.spring.security.authentication.handler.auth.UserLoginInfo;
import com.spring.security.domain.model.entity.Role;
import com.spring.security.domain.model.entity.User;
import com.spring.security.domain.model.entity.UserIdentity;
import com.spring.security.domain.model.entity.UserRole;
import com.spring.security.domain.repository.UserIdentityRepository;
import com.spring.security.domain.repository.UserRepository;
import com.spring.security.domain.repository.UserRoleRepository;
import com.spring.security.web.exception.UserNotFoundException;
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
    private final UserIdentityRepository userIdentityRepository;

    public User getUserByUsername(String username) {
        return userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found by username: " + username));
    }

    public User getUserByPhone(String phone) {
        return userRepository
                .findByPhone(phone)
                .orElseThrow(() -> new UserNotFoundException("User not found by phone: " + phone));
    }

    public User getUserByEmail(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found by email: " + email));
    }

    public User findById(Long userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found by id: " + userId));
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

    public UserLoginInfo loadUserByProviderUserIdAndProvider(Long providerUserId, UserIdentity.Provider provider) {
        User loadedUser = userIdentityRepository
                .findByProviderUserIdAndProvider(providerUserId, UserIdentity.Provider.GITHUB)
                .map(UserIdentity::getUserId)
                .flatMap(userRepository::findById)
                .orElse(null);
        if (loadedUser == null) {
            return null;
        }
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

    public UserLoginInfo loadUserByPhone(String phone) {
        User loadedUser = getUserByPhone(phone);
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
