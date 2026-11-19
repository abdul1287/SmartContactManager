package com.manager.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import com.manager.model.User;

public interface UserService {

    User getUser(String email);

    void registerUser(User user);

    User updateProfile(String currentEmail, User updates, MultipartFile profileImage) throws IOException;

    void changePassword(String email, String oldPassword, String newPassword);

    void resetPassword(String email, String newPassword);

    void deleteAccount(String email);
}
