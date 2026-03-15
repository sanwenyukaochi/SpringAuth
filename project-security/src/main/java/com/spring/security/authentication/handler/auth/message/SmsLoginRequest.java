package com.spring.security.authentication.handler.auth.message;

public record SmsLoginRequest(String phone, String captcha) {}
