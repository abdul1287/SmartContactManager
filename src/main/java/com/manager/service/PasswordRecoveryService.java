package com.manager.service;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;

public interface PasswordRecoveryService {

    void sendOtp(String email, HttpSession session) throws MessagingException;

    void verifyOtp(String otp, HttpSession session);

    void resetPassword(String newPassword, String confirmPassword, HttpSession session);
}
