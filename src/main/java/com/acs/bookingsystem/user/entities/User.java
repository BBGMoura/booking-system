package com.acs.bookingsystem.user.entities;

import com.acs.bookingsystem.authorization.entity.AuthUser;
import com.acs.bookingsystem.payment.Account;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name="Users")
public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1905122041950251207L;

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
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private AuthUser authUser;
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Account account;

    public User(String firstName, String lastName, String email, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }
}
