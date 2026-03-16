package com.spring.security.domain.model.dto;

import com.spring.security.web.enums.BaseCode;

public record Result<T>(String code, String message, T data) {

    public static <T> Result<T> success(T data) {
        return new Result<>(BaseCode.SUCCESS.getCode(), BaseCode.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> error(String code, String message, T data) {
        return new Result<>(code, message, data);
    }

    public static <T> Result<T> error(BaseCode baseCode, T data) {
        return new Result<>(baseCode.getCode(), baseCode.getMessage(), data);
    }
}
