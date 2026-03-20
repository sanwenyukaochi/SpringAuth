package com.spring.security.web.controller;

import com.spring.security.authentication.handler.auth.oneTimeToken.service.RedisOneTimeTokenService;
import com.spring.security.domain.model.entity.User;
import com.spring.security.domain.repository.UserRepository;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/public-api")
@RequiredArgsConstructor
public class PublicApiController {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RedisOneTimeTokenService redisOneTimeTokenService;

    @GetMapping
    public ResponseEntity<Map<String, String>> encode() {
        String encode = passwordEncoder.encode("admin");
        return ResponseEntity.ok(
                Map.of("passwordEncoder", Optional.ofNullable(encode).orElseThrow(IllegalArgumentException::new)));
    }

    @GetMapping("/user")
    public ResponseEntity<Page<@NonNull Map<String, Object>>> userPage(Pageable pageable) {
        Page<@NonNull User> userPage = userRepository.findAll(pageable);
        return ResponseEntity.ok(userPage.map(user -> Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "phone", user.getPhone())));
    }

    @PostMapping("/send-one-time-token")
    public ResponseEntity<?> sendOneTimeToken() {
        OneTimeToken ott = redisOneTimeTokenService.generate(
                new GenerateOneTimeTokenRequest("sanwenyukaochi", Duration.ofMinutes(5)));
        IO.println(ott.getTokenValue());
        return ResponseEntity.ok(null);
    }
}
