package com.acs.bookingsystem.booking.entities;

import com.acs.bookingsystem.booking.enums.ClassType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
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
}
