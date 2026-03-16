package org.oauth.client.service;

import org.oauth.client.dto.GitHubOAuthLoginRequest;
import org.oauth.client.dto.GitHubUserProfile;
import org.oauth.client.dto.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class SecurityLoginBridgeService {

    private static final ParameterizedTypeReference<Result<Object>> RESULT_TYPE =
            new ParameterizedTypeReference<>() {};

    private final RestClient restClient;

    public SecurityLoginBridgeService(
            RestClient.Builder restClientBuilder, @Value("${app.security-base-url}") String securityBaseUrl) {
        this.restClient = restClientBuilder.baseUrl(securityBaseUrl).build();
    }

    public Result<Object> githubLogin(GitHubUserProfile profile) {
        try {
            return restClient
                    .post()
                    .uri("/api/public-api/oauth/github/login")
                    .body(new GitHubOAuthLoginRequest(profile.id(), profile.login(), profile.name(), profile.email()))
                    .retrieve()
                    .body(RESULT_TYPE);
        } catch (RestClientException ex) {
            return new Result<>("oauth.bridge_error", "调用 project-security 失败: " + ex.getMessage(), null);
        }
    }
}
