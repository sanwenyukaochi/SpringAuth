package org.oauth.client.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.oauth.client.authentication.github.dto.GitHubOAuth2Meta;
import org.oauth.client.dto.GitHubUserProfile;
import org.oauth.client.dto.Result;
import org.oauth.client.service.SecurityLoginBridgeService;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GitHubLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final SecurityLoginBridgeService securityLoginBridgeService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        GitHubOAuth2Meta meta = (GitHubOAuth2Meta) authentication.getDetails();
        Result<Object> loginResult = securityLoginBridgeService.githubLogin(
                new GitHubUserProfile(meta.providerUserId(), meta.login(), meta.name(), meta.email()));
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), loginResult);
    }
}
