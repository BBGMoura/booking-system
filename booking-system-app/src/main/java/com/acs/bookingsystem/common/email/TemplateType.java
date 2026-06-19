package com.acs.bookingsystem.common.email;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TemplateType {
    INVITATION("invitation"),
    RESET_PASSWORD("resetPassword");

    private final String key;
}
