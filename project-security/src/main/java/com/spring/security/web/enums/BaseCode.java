package com.spring.security.web.enums;

import com.spring.security.web.exception.BaseException;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BaseCode {
    SUCCESS("success", "操作成功", HttpStatus.OK),
    SYSTEM_ERROR("common.system_error", "系统异常，请稍后重试", HttpStatus.INTERNAL_SERVER_ERROR),
    TYPE_ERROR("common.type_error", "类型错误", HttpStatus.BAD_REQUEST),
    HTTP_METHOD_NOT_ALLOWED("http.method_not_allowed", "请求方法不支持", HttpStatus.METHOD_NOT_ALLOWED),
    RESOURCE_NOT_FOUND("resource.not_found", "资源未找到", HttpStatus.NOT_FOUND),
    AUTHENTICATION_ERROR("authentication.error", "认证失败", HttpStatus.UNAUTHORIZED),
    AUTHENTICATION_TYPE_ERROR(
            "authentication.type_error",
            "登陆认证成功后，authentication.getPrincipal()返回的Object对象必须是：UserLoginInfo！",
            HttpStatus.INTERNAL_SERVER_ERROR),
    AUTH_LOGIN_FAILED("auth.login_failed", "登陆失败", HttpStatus.UNAUTHORIZED),
    AUTH_INVALID_CREDENTIALS("auth.invalid_credentials", "错误的凭证", HttpStatus.UNAUTHORIZED),
    TOKEN_MALFORMED("token.malformed", "JWT Token 无效", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("token.expired", "JWT 已过期", HttpStatus.UNAUTHORIZED),
    TOKEN_PARSE_ERROR("token.parse_error", "JWT 解析异常", HttpStatus.UNAUTHORIZED),
    TOKEN_UNSUPPORTED("token.unsupported", "JWT 不受支持", HttpStatus.UNAUTHORIZED),
    TOKEN_EMPTY("token.empty", "JWT 内容为空", HttpStatus.UNAUTHORIZED),
    TOKEN_NOT_FIND_IN_REDIS("token.not_found_in_redis", "JWT token was not found in Redis", HttpStatus.UNAUTHORIZED),
    AUTH_ACCESS_DENIED("auth.access_denied", "访问被拒绝", HttpStatus.FORBIDDEN),
    USER_NOT_FOUND("user.not_found", "用户未找到", HttpStatus.NOT_FOUND),
    USER_PHONE_NOT_FOUND("user.phone_not_found", "手机号未找到", HttpStatus.NOT_FOUND),
    USER_EMAIL_NOT_FOUND("user.email_not_found", "邮箱未找到", HttpStatus.NOT_FOUND),
    ;
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    public static BaseCode getByCode(String code) {
        return Arrays.stream(BaseCode.values())
                .filter(baseCode -> baseCode.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new BaseException(BaseCode.TYPE_ERROR));
    }

    public static String getByMessage(String code) {
        return getByCode(code).getMessage();
    }
}
