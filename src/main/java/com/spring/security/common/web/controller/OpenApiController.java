package com.spring.security.common.web.controller;

import com.spring.security.authentication.handler.auth.openApi.OpenApiLoginInfo;
import com.spring.security.domain.model.dto.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/open-api")
@RequiredArgsConstructor
public class OpenApiController {

    @GetMapping("/application-info")
    public Result<OpenApiLoginInfo> getOpenApiApplicationInfo(Authentication authentication) {
        OpenApiLoginInfo userLoginInfo = (OpenApiLoginInfo) authentication.getPrincipal();
        log.info("三方API登录信息：{}", userLoginInfo);
        return Result.success(userLoginInfo);
    }
}
