package com.acs.bookingsystem.booking;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Room {
    @Id
    @GeneratedValue
    private int id;
    @Column(length=25, nullable = false, unique = true)
    private String name;
    @Column(nullable = false)
    private LocalDateTime availableFrom;
    @Column(nullable = false)
    private LocalDateTime availableTo;
}
