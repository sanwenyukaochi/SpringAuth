package com.spring.security.authentication.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderEvent {
    PAY("支付"),
    SHIP("发货"),
    COMPLETE("完成"),
    CANCEL("取消"),
    APPLY_REFUND("申请退款"),
    CONFIRM_REFUND("确认退款"),
    ;
    private final String desc;
}
