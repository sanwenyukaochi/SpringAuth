package com.spring.security.web.controller;

import com.spring.security.authentication.handler.auth.UserLoginInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final JsonMapper jsonMapper;

    @GetMapping("/info")
    public ResponseEntity<UserLoginInfo> getUserInfo(Authentication authentication) {
        UserLoginInfo userLoginInfo = (UserLoginInfo) authentication.getPrincipal();
        log.info("用户登录信息：{}", jsonMapper.writeValueAsString(userLoginInfo));
        return ResponseEntity.ok(userLoginInfo);
    }

    @GetMapping("/details")
    public ResponseEntity<Object> getDetails(Authentication authentication) {
        return ResponseEntity.ok(authentication.getDetails());
    }

    @GetMapping("/principal")
    public ResponseEntity<Object> getPrincipal(Authentication authentication) {
        return ResponseEntity.ok(authentication.getPrincipal());
    }
}
