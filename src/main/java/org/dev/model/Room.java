package org.dev.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    @Column(name = "price_per_half_hour")
    private BigDecimal priceHalfHour;

    @Column(name = "price_per_hour")
    private BigDecimal priceHour;

    public Room() {}

    public Room(String name, BigDecimal priceHalfHour, BigDecimal priceHour) {

        this.name = name;
        this.priceHalfHour = priceHalfHour;
        this.priceHour = priceHour;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPriceHalfHour() {
        return priceHalfHour;
    }

    public BigDecimal getPriceHour() {
        return priceHour;
    }
}
