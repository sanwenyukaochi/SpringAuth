package com.spring.security.authentication.handler.auth.openApi;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

@Setter
@Getter
public class OpenApiAuthenticationToken extends AbstractAuthenticationToken {

    private String appId; // 前端传过来
    private String appSecurity; // 前端传过来
    private OpenApiLoginInfo currentUser; // 认证成功后，后台从数据库获取信息

    public OpenApiAuthenticationToken() {
        super(List.of());
    }

    @Override
    public Object getCredentials() {
        // 根据SpringSecurity的设计，授权成后，Credential（比如，登录密码）信息需要被清空
        return isAuthenticated() ? null : appSecurity;
    }

    @Override
    public Object getPrincipal() {
        // 根据SpringSecurity的设计，授权成功之前，getPrincipal返回的客户端传过来的数据。授权成功后，返回当前登陆用户的信息
        return isAuthenticated() ? currentUser : appId;
    }
}
