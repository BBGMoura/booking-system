package com.acs.bookingsystem.user.repository;

import com.acs.bookingsystem.user.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static com.acs.bookingsystem.TestDataUtil.createUser;
import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest(showSql = false)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveUser() {
        // Given
        User user = createUser();

        // When
        userRepository.save(user);

        // Then
        Optional<User> savedUser = userRepository.findByEmail(user.getEmail());
        assertTrue(savedUser.isPresent());
        assertEquals(user.getFirstName(),savedUser.get().getFirstName());
    }

    @Test
    void testFindById() {
        // Given
        User user = createUser();
        userRepository.save(user);

        // When
        Optional<User> foundUser = userRepository.findById(user.getId());

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals(user.getEmail(), foundUser.get().getEmail());
    }

    @Test
    void testFindByEmail() {
        // Given
        User user = createUser();
        userRepository.save(user);

        // When
        Optional<User> foundUser = userRepository.findByEmail(user.getEmail());

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals(user.getEmail(), foundUser.get().getEmail());
    }

    @Test
    void testFindByEmailNotFound() {
        // Given
        String email = "nonexistent@example.com";

        // When
        Optional<User> foundUser = userRepository.findByEmail(email);

        // Then
        assertFalse(foundUser.isPresent());
    }

    @Test
    void testUpdateUser() {
        // Given
        User user = createUser();
        userRepository.save(user);

        // When
        user.setEmail("updated@example.com");
        userRepository.save(user);

        // Then
        Optional<User> updatedUser = userRepository.findByEmail("updated@example.com");
        assertTrue(updatedUser.isPresent());
        assertEquals("updated@example.com", updatedUser.get().getEmail());
    }

}