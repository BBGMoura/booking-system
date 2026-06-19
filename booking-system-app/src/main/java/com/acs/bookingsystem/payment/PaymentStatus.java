package com.acs.bookingsystem.payment;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum PaymentStatus {
    OUTSTANDING,
    PAID,
    VOIDED
}
