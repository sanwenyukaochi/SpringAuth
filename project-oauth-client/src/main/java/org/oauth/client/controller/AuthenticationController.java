package org.oauth.client.controller;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationController {

    @GetMapping("/oauth/github/login")
    public void githubLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect("/api/login/oauth/github");
    }
}
