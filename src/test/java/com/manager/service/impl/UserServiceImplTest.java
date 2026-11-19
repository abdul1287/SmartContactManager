package com.manager.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.manager.exception.DuplicateUserException;
import com.manager.exception.InvalidPasswordException;
import com.manager.model.User;
import com.manager.repo.UserRepository;
import com.manager.service.ImageStorageService;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ImageStorageService imageStorageService;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, passwordEncoder, imageStorageService);
    }

    @Test
    void registrationSetsRoleAndStoresEncodedPassword() {
        User user = new User();
        user.setEmail("new@example.com");
        user.setPassword("plain-password");
        when(userRepository.findUserOptionalByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");

        userService.registerUser(user);

        assertEquals("ROLE_USER", user.getRole());
        assertEquals("encoded-password", user.getPassword());
        assertEquals(true, user.isEnabled());
        verify(userRepository).save(user);
    }

    @Test
    void registrationRejectsAnExistingEmail() {
        User user = new User();
        user.setEmail("existing@example.com");
        when(userRepository.findUserOptionalByEmail(user.getEmail())).thenReturn(Optional.of(new User()));

        assertThrows(DuplicateUserException.class, () -> userService.registerUser(user));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void passwordChangeRejectsIncorrectCurrentPassword() {
        User user = new User();
        user.setEmail("owner@example.com");
        user.setPassword("stored-hash");
        when(userRepository.findUserOptionalByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "stored-hash")).thenReturn(false);

        assertThrows(InvalidPasswordException.class,
                () -> userService.changePassword(user.getEmail(), "wrong-password", "new-password"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void passwordChangeRejectsTooShortNewPassword() {
        assertThrows(InvalidPasswordException.class,
                () -> userService.changePassword("owner@example.com", "current-password", "short"));

        verify(userRepository, never()).findUserOptionalByEmail(any(String.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void passwordResetStoresEncodedPassword() {
        User user = new User();
        user.setEmail("owner@example.com");
        when(userRepository.findUserOptionalByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-password");

        userService.resetPassword(user.getEmail(), "new-password");

        assertEquals("encoded-password", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void accountDeletionUsesTheAuthenticatedEmailLookup() {
        User user = new User();
        user.setEmail("owner@example.com");
        when(userRepository.findUserOptionalByEmail(user.getEmail())).thenReturn(Optional.of(user));

        userService.deleteAccount(user.getEmail());

        verify(userRepository).delete(user);
    }
}
