package com.manager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.manager.exception.DuplicateUserException;
import com.manager.helper.Message;
import com.manager.model.User;
import com.manager.service.UserService;

import jakarta.validation.Valid;

@Controller
public class HomeController {

    private final UserService userService;

    HomeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Home - Smart Contact Manager");
        return "home";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("title", "About - Smart Contact Manager");
        return "about";
    }

    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("title", "Sign Up - Smart Contact Manager");
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult results, Model model) {
        if (results.hasErrors()) {
            model.addAttribute("user", user);
            return "signup";
        }

        try {
            userService.registerUser(user);
            model.addAttribute("user", new User());
            model.addAttribute("message", new Message("Successfully registered!", "alert-success"));
        } catch (DuplicateUserException exception) {
            model.addAttribute("user", user);
            model.addAttribute("message",
                    new Message("User already registered with this email!", "alert-danger"));
        }
        return "signup";
    }

    @GetMapping("/signin")
    public String login(Model model) {
        model.addAttribute("title", "Login - Smart Contact Manager");
        return "login";
    }
}
