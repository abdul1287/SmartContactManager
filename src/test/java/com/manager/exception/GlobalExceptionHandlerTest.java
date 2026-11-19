package com.manager.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.manager.helper.Message;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void missingContactRedirectsToContactListWithMessage() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/user/contact/8");
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        Object result = exceptionHandler.handleContactNotFound(
                new ContactNotFoundException(), request, redirectAttributes);

        assertEquals("redirect:/user/show_contacts/0", result);
        assertEquals("Contact not found.",
                ((Message) redirectAttributes.getFlashAttributes().get("message")).getContent());
    }

    @Test
    void searchErrorsRemainJsonAndUseAnErrorStatus() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/user/search/example");

        Object result = exceptionHandler.handleUserNotFound(
                new UserNotFoundException(), request, new RedirectAttributesModelMap());

        ResponseEntity<?> response = assertInstanceOf(ResponseEntity.class, result);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(Map.of("error", "User not found."), response.getBody());
    }
}
