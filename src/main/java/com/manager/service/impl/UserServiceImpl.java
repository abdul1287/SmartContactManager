package com.manager.service.impl;

import java.io.IOException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.manager.exception.DuplicateUserException;
import com.manager.exception.InvalidPasswordException;
import com.manager.exception.UserNotFoundException;
import com.manager.model.User;
import com.manager.repo.UserRepository;
import com.manager.service.ImageStorageService;
import com.manager.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageStorageService imageStorageService;

    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ImageStorageService imageStorageService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.imageStorageService = imageStorageService;
    }

    @Override
    @Transactional(readOnly = true)
    public User getUser(String email) {
        return userRepository.findUserOptionalByEmail(email).orElseThrow(UserNotFoundException::new);
    }

    @Override
    @Transactional
    public void registerUser(User user) {
        if (userRepository.findUserOptionalByEmail(user.getEmail()).isPresent()) {
            throw new DuplicateUserException();
        }
        user.setRole("ROLE_USER");
        user.setEnabled(true);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateProfile(String currentEmail, User updates, MultipartFile profileImage) throws IOException {
        User user = getUser(currentEmail);
        User userWithEmail = userRepository.findUserOptionalByEmail(updates.getEmail()).orElse(null);
        if (userWithEmail != null && userWithEmail.getId() != user.getId()) {
            throw new DuplicateUserException();
        }

        user.setName(updates.getName());
        user.setEmail(updates.getEmail());
        user.setAbout(updates.getAbout());

        String storedImage = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            storedImage = imageStorageService.store(profileImage);
            user.setImageUrl(storedImage);
        }

        try {
            return userRepository.save(user);
        } catch (RuntimeException exception) {
            if (storedImage != null) {
                try {
                    imageStorageService.delete(storedImage);
                } catch (IOException cleanupException) {
                    exception.addSuppressed(cleanupException);
                }
            }
            throw exception;
        }
    }

    @Override
    @Transactional
    public void changePassword(String email, String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new InvalidPasswordException("New password must be at least 8 characters.");
        }

        User user = getUser(email);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new InvalidPasswordException("Old password is incorrect.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetPassword(String email, String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new InvalidPasswordException("New password must be at least 8 characters.");
        }
        User user = getUser(email);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteAccount(String email) {
        userRepository.delete(getUser(email));
    }
}
