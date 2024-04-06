package com.acs.bookingsystem.user.entities;

import com.acs.bookingsystem.payment.Account;
import com.acs.bookingsystem.user.enums.Permission;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;
    @Column(length=50, nullable = false)
    private String firstName;
    @Column(length=50, nullable=false)
    private String lastName;
    @Column(length=254, nullable=false, unique = true)
    private String email;
    @Column(length=11, nullable=false)
    private String phoneNumber;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Permission permission;
    @OneToOne(mappedBy = "user",cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Account account;

    public User(String firstName, String lastName, String email, String phoneNumber, Permission permission) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.permission = permission;
    }
}
