package com.acs.bookingsystem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;
    @Column(length=50, nullable = false)
    private String firstName;
    @Column(length=50, nullable=false)
    private String lastName;
    @Column(length=100, nullable=false, unique = true)
    private String email;
    @Column(length=15, nullable=false)
    private String phoneNumber;
    @Enumerated(EnumType.STRING)
    private Permission permission;
}
