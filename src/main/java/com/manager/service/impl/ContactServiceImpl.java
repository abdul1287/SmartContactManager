package com.manager.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.manager.exception.ContactNotFoundException;
import com.manager.exception.DuplicateContactException;
import com.manager.exception.UserNotFoundException;
import com.manager.model.Contact;
import com.manager.model.User;
import com.manager.repo.ContactRepository;
import com.manager.repo.UserRepository;
import com.manager.service.ContactService;
import com.manager.service.ImageStorageService;

@Service
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private final ImageStorageService imageStorageService;

    public ContactServiceImpl(
            ContactRepository contactRepository,
            UserRepository userRepository,
            ImageStorageService imageStorageService) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
        this.imageStorageService = imageStorageService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contact> getRecentContacts(String email, int limit) {
        List<Contact> contacts = contactRepository.findByUserId(getUser(email).getId());
        List<Contact> recentContacts = new ArrayList<>();
        for (int i = contacts.size() - 1; i >= 0 && recentContacts.size() < limit; i--) {
            recentContacts.add(contacts.get(i));
        }
        return recentContacts;
    }

    @Override
    @Transactional(readOnly = true)
    public long countContacts(String email) {
        return contactRepository.countByUser_Id(getUser(email).getId());
    }

    @Override
    @Transactional
    public Contact saveContact(String email, Contact submittedContact, MultipartFile profileImage) throws IOException {
        User user = getUser(email);
        boolean isNewContact = submittedContact.getCId() == 0;
        Contact contact;

        if (isNewContact) {
            contact = new Contact();
        } else {
            contact = contactRepository.findByCIdAndUser_Id(submittedContact.getCId(), user.getId())
                    .orElseThrow(ContactNotFoundException::new);
        }

        Contact duplicate = contactRepository.findByPhoneAndUser(submittedContact.getPhone(), user);
        if (duplicate != null && duplicate.getCId() != contact.getCId()) {
            throw new DuplicateContactException();
        }

        contact.setName(submittedContact.getName());
        contact.setNickname(submittedContact.getNickname());
        contact.setWork(submittedContact.getWork());
        contact.setEmail(submittedContact.getEmail());
        contact.setPhone(submittedContact.getPhone());
        contact.setDescription(submittedContact.getDescription());
        contact.setUser(user);

        String storedImage = null;
        if (profileImage == null || profileImage.isEmpty()) {
            if (isNewContact) {
                contact.setImage("default.png");
            }
        } else {
            storedImage = imageStorageService.store(profileImage);
            contact.setImage(storedImage);
        }

        try {
            return contactRepository.save(contact);
        } catch (RuntimeException exception) {
            if (storedImage != null) {
                try {
                    imageStorageService.delete(storedImage);
                } catch (IOException cleanupException) {
                    exception.addSuppressed(cleanupException);
                }
            }
            throw exception;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Contact> getContactPage(String email, int page, int size) {
        return contactRepository.findByUserId(getUser(email).getId(), PageRequest.of(page, size));
    }

    @Override
    @Transactional(readOnly = true)
    public Contact getContact(String email, int contactId) {
        User user = getUser(email);
        return contactRepository.findByCIdAndUser_Id(contactId, user.getId())
                .orElseThrow(ContactNotFoundException::new);
    }

    @Override
    @Transactional
    public void deleteContact(String email, int contactId) {
        User user = getUser(email);
        Contact contact = contactRepository.findByCIdAndUser_Id(contactId, user.getId())
                .orElseThrow(ContactNotFoundException::new);
        contactRepository.delete(contact);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contact> getContactsForUser(String email) {
        return contactRepository.findByUserId(getUser(email).getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contact> searchContacts(String email, String keyword) {
        return contactRepository.searchContacts(getUser(email).getId(), keyword);
    }

    private User getUser(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UserNotFoundException();
        }
        return user;
    }
}
