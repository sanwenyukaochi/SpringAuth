package com.spring.security.authentication.handler.auth.oneTimeToken.service;

import com.spring.security.authentication.handler.auth.oneTimeToken.OneTimeTokenAuthenticationToken;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest;
import org.springframework.security.authentication.ott.OneTimeToken;

public interface OneTimeTokenService {

    OneTimeToken generate(GenerateOneTimeTokenRequest request);

    @Nullable OneTimeToken consume(OneTimeTokenAuthenticationToken authenticationToken);
}
