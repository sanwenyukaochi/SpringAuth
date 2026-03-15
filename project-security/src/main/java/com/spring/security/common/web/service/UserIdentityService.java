package com.spring.security.common.web.service;

import com.spring.security.common.web.enums.BaseCode;
import com.spring.security.common.web.exception.BaseException;
import com.spring.security.domain.model.entity.UserIdentity;
import com.spring.security.domain.repository.UserIdentityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserIdentityService {

    private final UserIdentityRepository userIdentityRepository;

    public UserIdentity getUserIdentityByProviderUserIdAndProvider(
            Long providerUserId, UserIdentity.Provider provider) {
        return userIdentityRepository
                .findByProviderUserIdAndProvider(providerUserId, provider)
                .orElseThrow(() -> new BaseException(BaseCode.USER_NOT_FOUND));
    }
}
