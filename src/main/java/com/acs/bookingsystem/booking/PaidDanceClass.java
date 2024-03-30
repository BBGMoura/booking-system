package com.acs.bookingsystem.booking;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("paid")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaidDanceClass extends DanceClass {
    @Column(nullable = false)
    private BigDecimal pricePer60;
    @Column(nullable = false)
    private BigDecimal pricePer45;
    @Column(nullable = false)
    private BigDecimal pricePer30;
}
