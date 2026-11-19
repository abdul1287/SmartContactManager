package com.manager.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.manager.model.User;

class CustomUserDetailTest {

    @Test
    void enabledStatusReflectsUserEntity() {
        User user = new User();
        CustomUserDetail userDetails = new CustomUserDetail(user);

        assertFalse(userDetails.isEnabled());

        user.setEnabled(true);
        assertTrue(userDetails.isEnabled());
    }
}
