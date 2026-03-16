package com.spring.security.web.service;

import com.spring.security.web.enums.BaseCode;
import com.spring.security.web.exception.BaseException;
import com.spring.security.web.model.entity.UserIdentity;
import com.spring.security.web.repository.UserIdentityRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserIdentityService {

    private final UserIdentityRepository userIdentityRepository;

    public Optional<UserIdentity> findByProviderUserIdAndProvider(Long providerUserId, UserIdentity.Provider provider) {
        return userIdentityRepository.findByProviderUserIdAndProvider(providerUserId, provider);
    }

    public UserIdentity getUserIdentityByProviderUserIdAndProvider(
            Long providerUserId, UserIdentity.Provider provider) {
        return findByProviderUserIdAndProvider(providerUserId, provider)
                .orElseThrow(() -> new BaseException(BaseCode.USER_NOT_FOUND));
    }
}
