package com.manager.service;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.manager.model.Contact;

public interface ContactService {

    List<Contact> getRecentContacts(String email, int limit);

    long countContacts(String email);

    Contact saveContact(String email, Contact contact, MultipartFile profileImage) throws IOException;

    Page<Contact> getContactPage(String email, int page, int size);

    Contact getContact(String email, int contactId);

    void deleteContact(String email, int contactId);

    List<Contact> getContactsForUser(String email);

    List<Contact> searchContacts(String email, String keyword);
}
