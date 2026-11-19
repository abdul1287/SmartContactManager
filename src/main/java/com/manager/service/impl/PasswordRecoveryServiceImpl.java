package com.manager.service.impl;

import java.security.SecureRandom;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.manager.exception.OtpVerificationException;
import com.manager.exception.PasswordsDoNotMatchException;
import com.manager.model.User;
import com.manager.service.EmailService;
import com.manager.service.PasswordRecoveryService;
import com.manager.service.UserService;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;

@Service
public class PasswordRecoveryServiceImpl implements PasswordRecoveryService {

    private static final String CHALLENGE_SESSION_KEY = "passwordRecoveryChallenge";
    private static final Duration OTP_VALIDITY = Duration.ofMinutes(5);
    private static final int MAX_ATTEMPTS = 5;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserService userService;
    private final EmailService emailService;
    private final Clock clock;

    @Autowired
    public PasswordRecoveryServiceImpl(UserService userService, EmailService emailService) {
        this(userService, emailService, Clock.systemUTC());
    }

    PasswordRecoveryServiceImpl(UserService userService, EmailService emailService, Clock clock) {
        this.userService = userService;
        this.emailService = emailService;
        this.clock = clock;
    }

    @Override
    public void sendOtp(String email, HttpSession session) throws MessagingException {
        session.removeAttribute(CHALLENGE_SESSION_KEY);
        User user = userService.getUser(email);
        String otp = Integer.toString(100000 + SECURE_RANDOM.nextInt(900000));
        String message = "<h2>Smart Contact Manager</h2>"
                + "<p>Your One-Time Password (OTP) is:</p>"
                + "<h1 style='color:blue;'>" + otp + "</h1>"
                + "<p>This OTP is valid for 5 minutes.</p>"
                + "<p>Please do not share it with anyone.</p>";

        emailService.sendEmail(message, user.getEmail());
        session.setAttribute(
                CHALLENGE_SESSION_KEY,
                new OtpChallenge(user.getEmail(), otp, clock.instant().plus(OTP_VALIDITY), 0, false));
    }

    @Override
    public void verifyOtp(String otp, HttpSession session) {
        OtpChallenge challenge = requireActiveChallenge(session);
        if (challenge.verified()) {
            return;
        }

        if (!matchesOtp(challenge.otp(), otp)) {
            int attempts = challenge.attempts() + 1;
            if (attempts >= MAX_ATTEMPTS) {
                session.removeAttribute(CHALLENGE_SESSION_KEY);
                throw new OtpVerificationException("Too many incorrect attempts. Request a new code.");
            }
            session.setAttribute(CHALLENGE_SESSION_KEY, challenge.withAttempts(attempts));
            throw new OtpVerificationException("Incorrect OTP. Please try again.");
        }

        session.setAttribute(CHALLENGE_SESSION_KEY, challenge.withVerified());
    }

    @Override
    public void resetPassword(String newPassword, String confirmPassword, HttpSession session) {
        OtpChallenge challenge = requireActiveChallenge(session);
        if (!challenge.verified()) {
            throw new OtpVerificationException("Verify your OTP before resetting the password.");
        }
        if (!Objects.equals(newPassword, confirmPassword)) {
            throw new PasswordsDoNotMatchException();
        }

        userService.resetPassword(challenge.email(), newPassword);
        session.removeAttribute(CHALLENGE_SESSION_KEY);
    }

    private OtpChallenge requireActiveChallenge(HttpSession session) {
        Object value = session.getAttribute(CHALLENGE_SESSION_KEY);
        if (!(value instanceof OtpChallenge challenge)) {
            throw new OtpVerificationException("OTP challenge is missing. Request a new code.");
        }
        if (!clock.instant().isBefore(challenge.expiresAt())) {
            session.removeAttribute(CHALLENGE_SESSION_KEY);
            throw new OtpVerificationException("OTP has expired. Request a new code.");
        }
        return challenge;
    }

    private boolean matchesOtp(String expected, String actual) {
        if (actual == null || !actual.matches("\\d{6}")) {
            return false;
        }
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.US_ASCII),
                actual.getBytes(StandardCharsets.US_ASCII));
    }

    private record OtpChallenge(String email, String otp, Instant expiresAt, int attempts, boolean verified) {

        private OtpChallenge withAttempts(int newAttempts) {
            return new OtpChallenge(email, otp, expiresAt, newAttempts, verified);
        }

        private OtpChallenge withVerified() {
            return new OtpChallenge(email, otp, expiresAt, attempts, true);
        }
    }
}
