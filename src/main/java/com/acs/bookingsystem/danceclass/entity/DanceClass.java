package com.acs.bookingsystem.danceclass.entity;

import com.acs.bookingsystem.danceclass.enums.ClassType;
import com.acs.bookingsystem.user.enums.Role;
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

    @Enumerated(EnumType.STRING)
    private Role role;

    public DanceClass(ClassType classType, boolean active, BigDecimal pricePerHour, Role role) {
        this.classType = classType;
        this.active = active;
        this.pricePerHour = pricePerHour;
        this.role = role;
    }
}
