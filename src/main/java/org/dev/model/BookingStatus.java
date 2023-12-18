package org.dev.model;

public enum BookingStatus {
    PENDING("P"),
    BOOKED("B"),
    CANCELLED("C");

    private final String code;

    private BookingStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
