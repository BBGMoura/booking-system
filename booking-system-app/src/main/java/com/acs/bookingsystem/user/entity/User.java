package com.acs.bookingsystem.user.entity;

import com.acs.bookingsystem.user.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false, updatable = false)
  private UUID uid;

  @Version private Long version;

  @Column(unique = true, nullable = false)
  private String email;

  @JsonIgnore @ToString.Exclude private String password;

  @Enumerated(EnumType.STRING)
  private Role role;

  @Column(nullable = false)
  private boolean locked;

  @Column(nullable = false)
  private boolean enabled;

  @Column(length = 50)
  private String firstName;

  @Column(length = 50)
  private String lastName;

  @Column(length = 11)
  private String phoneNumber;

  @PrePersist
  void prePersist() {
    if (uid == null) {
      uid = UUID.randomUUID();
    }
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singletonList(new SimpleGrantedAuthority(role.name()));
  }

  @Override
  public String getUsername() {
    return this.email;
  }

  @Override
  public boolean isAccountNonLocked() {
    return !this.locked;
  }

  @Override
  public boolean isEnabled() {
    return this.enabled;
  }
}
