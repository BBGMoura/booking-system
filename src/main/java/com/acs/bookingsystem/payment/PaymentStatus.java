package com.acs.bookingsystem.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentStatus {
    OUTSTANDING("O"),
    PAID("P"),
    VOIDED("V");

    private final String code;
}
