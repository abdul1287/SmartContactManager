package com.manager.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.manager.exception.ContactNotFoundException;
import com.manager.exception.DuplicateContactException;
import com.manager.exception.DuplicateUserException;
import com.manager.exception.InvalidImageException;
import com.manager.exception.InvalidPasswordException;
import com.manager.helper.Message;
import com.manager.model.Contact;
import com.manager.model.User;
import com.manager.service.ContactService;
import com.manager.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/user")
public class UserController {

    private final ContactService contactService;
    private final UserService userService;

    UserController(ContactService contactService, UserService userService) {
        this.contactService = contactService;
        this.userService = userService;
    }

    @RequestMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        User user = userService.getUser(principal.getName());
        model.addAttribute("contacts", contactService.getRecentContacts(principal.getName(), 5));
        model.addAttribute("totalContacts", contactService.countContacts(principal.getName()));
        model.addAttribute("user", user);
        return "normal/user_dashboard";
    }

    @RequestMapping("/add_contact")
    public String addContact(Model model, Principal principal) {
        User user = userService.getUser(principal.getName());
        model.addAttribute("user", user);
        model.addAttribute("contact", new Contact());
        return "normal/add_contact_form";
    }

    @PostMapping("/process-contact")
    public String processContact(
            @Valid @ModelAttribute Contact contact,
            BindingResult result,
            @RequestParam(value = "profileImage", required = false) MultipartFile file,
            Principal principal,
            Model model) {

        User user = userService.getUser(principal.getName());
        if (result.hasErrors()) {
            model.addAttribute("user", user);
            return "normal/add_contact_form";
        }

        boolean isNewContact = contact.getCId() == 0;
        try {
            contactService.saveContact(principal.getName(), contact, file);
            model.addAttribute("contact", new Contact());
            String message = isNewContact ? "Contact added successfully!" : "Contact updated successfully!";
            model.addAttribute("message",
                    new Message(message, "alert-success"));
        } catch (DuplicateContactException | InvalidImageException exception) {
            model.addAttribute("contact", contact);
            model.addAttribute("message", new Message(exception.getMessage(), "alert-danger"));
        } catch (ContactNotFoundException exception) {
            model.addAttribute("contact", contact);
            model.addAttribute("message",
                    new Message("The contact could not be found or is no longer available.", "alert-danger"));
        } catch (IOException exception) {
            model.addAttribute("contact", contact);
            model.addAttribute("message",
                    new Message("The contact image could not be saved. Please try again.", "alert-danger"));
        }

        model.addAttribute("user", user);
        return "normal/add_contact_form";
    }

    @GetMapping("/show_contacts/{page}")
    public String showContacts(@PathVariable("page") int page, Model model, Principal principal) {
        User user = userService.getUser(principal.getName());
        Page<Contact> contacts = contactService.getContactPage(principal.getName(), page, 8);
        model.addAttribute("user", user);
        model.addAttribute("contacts", contacts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", contacts.getTotalPages());
        return "normal/show_contacts";
    }

    @GetMapping("/contact/{cId}")
    public String showContactDetail(
            @PathVariable("cId") int cId,
            Model model,
            Principal principal) {
        User user = userService.getUser(principal.getName());
        model.addAttribute("contact", contactService.getContact(principal.getName(), cId));
        model.addAttribute("user", user);
        return "normal/contact_detail";
    }

    @PostMapping("/delete/{cId}")
    public String deleteContact(
            @PathVariable("cId") int cId,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        contactService.deleteContact(principal.getName(), cId);
        redirectAttributes.addFlashAttribute(
                "message",
                new Message("Contact deleted successfully!", "alert-success"));
        return "redirect:/user/show_contacts/0";
    }

    @GetMapping("/update-contact/{cId}")
    public String updateContact(
            @PathVariable("cId") int cId,
            Model model,
            Principal principal) {
        User user = userService.getUser(principal.getName());
        Contact contact = contactService.getContact(principal.getName(), cId);
        model.addAttribute("contact", contact);
        model.addAttribute("user", user);
        return "normal/add_contact_form";
    }

    @GetMapping("/profile")
    public String profileHandler(Model model, Principal principal) {
        User user = userService.getUser(principal.getName());
        List<Contact> contacts = contactService.getContactsForUser(principal.getName());
        model.addAttribute("contacts", contacts);
        model.addAttribute("user", user);
        return "normal/profile";
    }

    @GetMapping("/profile/edit-profile")
    public String editUserProfile(Model model, Principal principal) {
        model.addAttribute("user", userService.getUser(principal.getName()));
        return "normal/edit_profile";
    }

    @PostMapping("/profile/process-update-profile")
    public String updateUserProfile(
            @Valid @ModelAttribute User user,
            BindingResult result,
            @RequestParam(value = "profileImage", required = false) MultipartFile file,
            Model model,
            Principal principal,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        User currentUser = userService.getUser(principal.getName());
        if (result.hasErrors()) {
            user.setImageUrl(currentUser.getImageUrl());
            model.addAttribute("user", user);
            return "normal/edit_profile";
        }

        try {
            User updatedUser = userService.updateProfile(principal.getName(), user, file);
            if (!updatedUser.getEmail().equals(principal.getName())) {
                if (request.getSession(false) != null) {
                    request.getSession(false).invalidate();
                }
                redirectAttributes.addFlashAttribute(
                        "message",
                        new Message("Profile updated. Please sign in again with your updated email.", "alert-success"));
                return "redirect:/signin";
            }
            model.addAttribute("user", updatedUser);
            model.addAttribute("contacts", contactService.getContactsForUser(updatedUser.getEmail()));
            model.addAttribute("message",
                    new Message("Profile updated successfully!", "alert-success"));
            return "normal/profile";
        } catch (DuplicateUserException | InvalidImageException exception) {
            user.setImageUrl(currentUser.getImageUrl());
            model.addAttribute("user", user);
            model.addAttribute("message", new Message(exception.getMessage(), "alert-danger"));
        } catch (IOException exception) {
            user.setImageUrl(currentUser.getImageUrl());
            model.addAttribute("user", user);
            model.addAttribute("message",
                    new Message("The profile image could not be saved. Please try again.", "alert-danger"));
        }
        return "normal/edit_profile";
    }

    @GetMapping("/profile/change-password")
    public String changePassword(Model model, Principal principal) {
        model.addAttribute("user", userService.getUser(principal.getName()));
        return "normal/change_password";
    }

    @PostMapping("/profile/change-password-process")
    public String changePasswordProcess(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            userService.changePassword(principal.getName(), oldPassword, newPassword);
            redirectAttributes.addFlashAttribute(
                    "message",
                    new Message("Password changed successfully!", "alert-success"));
        } catch (InvalidPasswordException exception) {
            redirectAttributes.addFlashAttribute(
                    "message",
                    new Message(exception.getMessage(), "alert-danger"));
            return "redirect:/user/profile/change-password";
        }
        return "redirect:/user/profile";
    }

    @PostMapping("/profile/delete/{id}")
    public String deleteUser(
            @PathVariable("id") int ignoredUserId,
            Principal principal,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        userService.deleteAccount(principal.getName());
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        redirectAttributes.addFlashAttribute(
                "message",
                new Message("Account deleted successfully.", "alert-success"));
        return "redirect:/signin";
    }
}
