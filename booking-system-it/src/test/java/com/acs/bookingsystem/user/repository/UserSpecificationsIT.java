package com.acs.bookingsystem.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.acs.bookingsystem.BaseIntegrationTest;
import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.enums.Role;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class UserSpecificationsIT extends BaseIntegrationTest {

  @Autowired private UserRepository userRepository;

  @BeforeEach
  void setup() {
    userRepository.deleteAll();
    save("jane@studio.com", "Jane", "Cooper");
    save("bob@studio.com", "Bob", "Smith");
    save("invited@studio.com", null, null);
  }

  @Test
  void search_matchesFirstNameIgnoringCase() {
    assertThat(emailsFor("JAN")).containsExactly("jane@studio.com");
  }

  @Test
  void search_matchesLastNameIgnoringCase() {
    assertThat(emailsFor("coo")).containsExactly("jane@studio.com");
  }

  @Test
  void search_matchesEmailIgnoringCase() {
    assertThat(emailsFor("JANE@")).containsExactly("jane@studio.com");
  }

  @Test
  void search_trimsSurroundingSpaces() {
    assertThat(emailsFor("  coo  ")).containsExactly("jane@studio.com");
  }

  @Test
  void search_findsInvitedUserWithoutNameByEmail() {
    assertThat(emailsFor("invited")).containsExactly("invited@studio.com");
  }

  @Test
  void search_treatsLikeWildcardsLiterally() {
    assertThat(emailsFor("%")).isEmpty();
    assertThat(emailsFor("_")).isEmpty();
  }

  @Test
  void emptyOrMissingSearch_returnsEveryoneIncludingInvitedUsers() {
    assertThat(emailsFor(null)).hasSize(3);
    assertThat(emailsFor("")).hasSize(3);
    assertThat(emailsFor("   ")).hasSize(3);
  }

  private List<String> emailsFor(String search) {
    return userRepository
        .findAll(
            UserSpecifications.matchesSearch(search),
            PageRequest.of(0, 10, Sort.by("lastName", "firstName")))
        .map(User::getEmail)
        .getContent();
  }

  private void save(String email, String firstName, String lastName) {
    userRepository.save(
        User.builder()
            .uid(UUID.randomUUID())
            .email(email)
            .firstName(firstName)
            .lastName(lastName)
            .role(Role.ROLE_USER)
            .enabled(true)
            .build());
  }
}
