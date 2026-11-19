package com.manager.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.manager.helper.Message;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ContactNotFoundException.class)
    public Object handleContactNotFound(
            ContactNotFoundException exception,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        if (isSearchRequest(request)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Contact not found."));
        }
        redirectAttributes.addFlashAttribute("message",
                new Message("Contact not found.", "alert-danger"));
        return "redirect:/user/show_contacts/0";
    }

    @ExceptionHandler(UserNotFoundException.class)
    public Object handleUserNotFound(
            UserNotFoundException exception,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        if (isSearchRequest(request)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found."));
        }
        redirectAttributes.addFlashAttribute("message",
                new Message("Your account is no longer available. Please sign in again.", "alert-danger"));
        return "redirect:/signin";
    }

    private boolean isSearchRequest(HttpServletRequest request) {
        return request.getRequestURI().contains("/user/search/");
    }
}
