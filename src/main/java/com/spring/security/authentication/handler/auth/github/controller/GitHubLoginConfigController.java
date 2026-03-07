package com.spring.security.authentication.handler.auth.github.controller;

import com.spring.security.authentication.handler.auth.github.dto.GitHubOAuthConfigResponse;
import com.spring.security.domain.model.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/login/github")
@RequiredArgsConstructor
public class GitHubLoginConfigController {

    private final ClientRegistrationRepository registrationRepository;

    @GetMapping("/config")
    public Result<GitHubOAuthConfigResponse> config() {
        ClientRegistration registration = registrationRepository.findByRegistrationId("github");
        GitHubOAuthConfigResponse data =
                new GitHubOAuthConfigResponse(registration.getClientId(), registration.getRedirectUri());
        return Result.success(data);
    }
}
