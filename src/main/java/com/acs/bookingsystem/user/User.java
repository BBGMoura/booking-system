package com.acs.bookingsystem.user;

import com.acs.bookingsystem.payment.Account;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="Users")
public class User {
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
    @Column(nullable = false)
    private Permission permission;
    @OneToOne(mappedBy = "user",cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Account account;
}
