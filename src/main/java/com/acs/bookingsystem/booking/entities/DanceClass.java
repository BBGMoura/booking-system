package com.acs.bookingsystem.booking.entities;

import com.acs.bookingsystem.booking.enums.ClassType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DanceClass {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;
    @Enumerated(EnumType.STRING)
    private ClassType classType;
    @Column(nullable=false)
    private boolean active;
    private BigDecimal pricePer60;
    private BigDecimal pricePer45;
    private BigDecimal pricePer30;

    public DanceClass(ClassType classType, boolean active, BigDecimal pricePer60, BigDecimal pricePer45, BigDecimal pricePer30) {
        this.classType = classType;
        this.active = active;
        this.pricePer60 = pricePer60;
        this.pricePer45 = pricePer45;
        this.pricePer30 = pricePer30;
    }
}
