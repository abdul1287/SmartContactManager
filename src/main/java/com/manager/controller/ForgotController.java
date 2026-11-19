package com.manager.controller;

import org.springframework.mail.MailException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.manager.exception.InvalidPasswordException;
import com.manager.exception.OtpVerificationException;
import com.manager.exception.PasswordsDoNotMatchException;
import com.manager.exception.UserNotFoundException;
import com.manager.helper.Message;
import com.manager.service.PasswordRecoveryService;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {

    private final PasswordRecoveryService passwordRecoveryService;

    ForgotController(PasswordRecoveryService passwordRecoveryService) {
        this.passwordRecoveryService = passwordRecoveryService;
    }

    @GetMapping("/forgot")
    public String openEmailForm() {
        return "forgot_email_form";
    }

    @PostMapping("/send-otp")
    public String sendOtp(@RequestParam("email") String email, Model model, HttpSession session) {
        try {
            passwordRecoveryService.sendOtp(email, session);
            return "verify_otp";
        } catch (UserNotFoundException exception) {
            model.addAttribute("message",
                    new Message("User does not exist with this email.", "alert-danger"));
        } catch (MessagingException | MailException exception) {
            model.addAttribute("message",
                    new Message("Failed to send OTP. Please try again.", "alert-danger"));
        }
        return "forgot_email_form";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam("otp") String otp, HttpSession session, Model model) {
        try {
            passwordRecoveryService.verifyOtp(otp, session);
            return "reset_password_form";
        } catch (OtpVerificationException exception) {
            model.addAttribute("message", new Message(exception.getMessage(), "alert-danger"));
            return "verify_otp";
        }
    }

    @PostMapping("/reset-password-process")
    public String resetPassword(
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model,
            HttpSession session) {
        try {
            passwordRecoveryService.resetPassword(newPassword, confirmPassword, session);
            session.setAttribute("message",
                    new Message("Password changed successfully.", "alert-success"));
            return "redirect:/signin?reset=reset%20password%20successfully";
        } catch (PasswordsDoNotMatchException | InvalidPasswordException | OtpVerificationException exception) {
            model.addAttribute("message", new Message(exception.getMessage(), "alert-danger"));
            return "reset_password_form";
        }
    }
}
