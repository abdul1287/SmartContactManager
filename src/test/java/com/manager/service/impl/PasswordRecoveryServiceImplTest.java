package com.manager.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;

import com.manager.exception.OtpVerificationException;
import com.manager.model.User;
import com.manager.service.EmailService;
import com.manager.service.UserService;

@ExtendWith(MockitoExtension.class)
class PasswordRecoveryServiceImplTest {

    private static final String EMAIL = "owner@example.com";
    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

    private PasswordRecoveryServiceImpl passwordRecoveryService;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        passwordRecoveryService = new PasswordRecoveryServiceImpl(
                userService, emailService, Clock.fixed(NOW, ZoneOffset.UTC));
        session = new MockHttpSession();
        User user = new User();
        user.setEmail(EMAIL);
        when(userService.getUser(EMAIL)).thenReturn(user);
    }

    @Test
    void validOtpAuthorizesResetAndIsRemovedAfterward() throws Exception {
        passwordRecoveryService.sendOtp(EMAIL, session);
        String otp = sentOtp();

        passwordRecoveryService.verifyOtp(otp, session);
        passwordRecoveryService.resetPassword("new-password", "new-password", session);

        verify(userService).resetPassword(EMAIL, "new-password");
        assertThrows(OtpVerificationException.class,
                () -> passwordRecoveryService.verifyOtp(otp, session));
    }

    @Test
    void otpExpiresAfterFiveMinutes() throws Exception {
        passwordRecoveryService.sendOtp(EMAIL, session);
        PasswordRecoveryServiceImpl expiredService = new PasswordRecoveryServiceImpl(
                userService, emailService, Clock.fixed(NOW.plusSeconds(301), ZoneOffset.UTC));

        assertThrows(OtpVerificationException.class, () -> expiredService.verifyOtp(sentOtp(), session));
    }

    @Test
    void fifthIncorrectAttemptInvalidatesChallenge() throws Exception {
        passwordRecoveryService.sendOtp(EMAIL, session);

        for (int attempt = 0; attempt < 5; attempt++) {
            assertThrows(OtpVerificationException.class,
                    () -> passwordRecoveryService.verifyOtp("000000", session));
        }

        assertThrows(OtpVerificationException.class,
                () -> passwordRecoveryService.verifyOtp(sentOtp(), session));
    }

    private String sentOtp() throws Exception {
        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendEmail(message.capture(), eq(EMAIL));
        Matcher matcher = Pattern.compile("<h1[^>]*>(\\d{6})</h1>").matcher(message.getValue());
        if (!matcher.find()) {
            throw new AssertionError("Email did not contain a six-digit OTP.");
        }
        return matcher.group(1);
    }
}
