package com.spring.security.web.exception;

import com.spring.security.web.enums.BaseCode;
import com.spring.security.web.model.dto.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class WebGlobalExceptionHandler {

    @ExceptionHandler(value = BaseException.class)
    public Result<?> exceptionHandler(HttpServletRequest request, HttpServletResponse response, BaseException e) {
        response.setStatus(e.getHttpStatus().value());
        logWarn("业务异常", e.getCode(), e.getMessage(), request, e);
        return Result.error(e.getCode(), e.getMessage(), null);
    }

    @ExceptionHandler(value = Exception.class)
    public Result<?> exceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception e) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        logWarn("系统异常", BaseCode.SYSTEM_ERROR.getCode(), e.getMessage(), request, e);
        return Result.error(BaseCode.SYSTEM_ERROR, null);
    }

    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public Result<?> exceptionHandler(
            HttpServletRequest request, HttpServletResponse response, HttpRequestMethodNotSupportedException e) {
        response.setStatus(HttpStatus.METHOD_NOT_ALLOWED.value());
        logWarn("请求方法异常", BaseCode.HTTP_METHOD_NOT_ALLOWED.getCode(), e.getMessage(), request, e);
        return Result.error(
                BaseCode.HTTP_METHOD_NOT_ALLOWED.getCode(),
                BaseCode.HTTP_METHOD_NOT_ALLOWED.getMessage() + e.getMethod(),
                null);
    }

    @ExceptionHandler(value = NoResourceFoundException.class)
    public Result<?> exceptionHandler(
            HttpServletRequest request, HttpServletResponse response, NoResourceFoundException e) {
        response.setStatus(HttpStatus.NOT_FOUND.value());
        logWarn("请求资源异常", BaseCode.RESOURCE_NOT_FOUND.getCode(), e.getMessage(), request, e);
        return Result.error(BaseCode.RESOURCE_NOT_FOUND.getCode(), e.getMessage(), null);
    }

    private void logWarn(String biz, String code, String message, HttpServletRequest request, Throwable e) {
        log.warn(
                "{} code={}, 错误信息={}, method={}, url={}, query={}",
                biz,
                code,
                message,
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString(),
                e);
    }
}
