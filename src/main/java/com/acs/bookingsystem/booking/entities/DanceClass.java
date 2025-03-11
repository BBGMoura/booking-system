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
    private BigDecimal pricePerHour;

    public DanceClass(ClassType classType, boolean active, BigDecimal pricePerHour) {
        this.classType = classType;
        this.active = active;
        this.pricePerHour = pricePerHour;
    }
}
