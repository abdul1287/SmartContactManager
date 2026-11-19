package com.manager.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.manager.exception.ContactNotFoundException;
import com.manager.exception.DuplicateContactException;
import com.manager.exception.InvalidImageException;
import com.manager.model.Contact;
import com.manager.model.User;
import com.manager.repo.ContactRepository;
import com.manager.repo.UserRepository;
import com.manager.service.ImageStorageService;

@ExtendWith(MockitoExtension.class)
class ContactServiceImplTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImageStorageService imageStorageService;

    private ContactServiceImpl contactService;
    private User owner;

    @BeforeEach
    void setUp() {
        contactService = new ContactServiceImpl(contactRepository, userRepository, imageStorageService);
        owner = new User();
        owner.setId(1);
        owner.setEmail("owner@example.com");
        when(userRepository.findByEmail(owner.getEmail())).thenReturn(owner);
    }

    @Test
    void updateRejectsContactOwnedByAnotherUser() {
        Contact submitted = new Contact();
        submitted.setCId(7);
        submitted.setName("Changed");

        when(contactRepository.findByCIdAndUser_Id(7, owner.getId())).thenReturn(Optional.empty());

        assertThrows(ContactNotFoundException.class,
                () -> contactService.saveContact(owner.getEmail(), submitted, null));

        verify(contactRepository, never()).save(any(Contact.class));
    }

    @Test
    void saveRejectsDuplicatePhoneForSameOwner() {
        Contact existing = new Contact();
        existing.setCId(9);
        Contact submitted = new Contact();
        submitted.setPhone("1234567890");

        when(contactRepository.findByPhoneAndUser(submitted.getPhone(), owner)).thenReturn(existing);

        assertThrows(DuplicateContactException.class,
                () -> contactService.saveContact(owner.getEmail(), submitted, null));

        verify(contactRepository, never()).save(any(Contact.class));
    }

    @Test
    void saveSetsOwnerAndDefaultImageForNewContact() throws Exception {
        Contact submitted = new Contact();
        submitted.setName("A Contact");
        submitted.setPhone("1234567890");
        when(contactRepository.save(any(Contact.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Contact saved = contactService.saveContact(owner.getEmail(), submitted, null);

        assertEquals(owner, saved.getUser());
        assertEquals("default.png", saved.getImage());
    }

    @Test
    void saveRejectsInvalidImage() throws Exception {
        Contact submitted = new Contact();
        submitted.setPhone("1234567890");
        MockMultipartFile image = new MockMultipartFile(
                "profileImage", "payload.txt", "text/plain", "not an image".getBytes());
        when(imageStorageService.store(image)).thenThrow(new InvalidImageException("Invalid image."));

        assertThrows(InvalidImageException.class,
                () -> contactService.saveContact(owner.getEmail(), submitted, image));

        verify(contactRepository, never()).save(any(Contact.class));
    }
}
