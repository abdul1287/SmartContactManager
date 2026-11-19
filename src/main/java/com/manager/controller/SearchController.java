package com.manager.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import com.manager.model.Contact;
import com.manager.service.ContactService;

@RestController
@RequestMapping("/user")
public class SearchController {

    private final ContactService contactService;

    SearchController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping("/search/{keyword}")
    public ResponseEntity<List<Contact>> searchHandler(@PathVariable String keyword, Principal principal) {

        List<Contact> contacts = contactService.searchContacts(principal.getName(), keyword);
        return ResponseEntity.ok(contacts);
    }
}
