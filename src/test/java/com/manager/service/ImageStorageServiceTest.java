package com.manager.service;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import javax.imageio.ImageIO;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.util.unit.DataSize;

import com.manager.exception.InvalidImageException;

class ImageStorageServiceTest {

    @TempDir
    Path uploadDirectory;

    private ImageStorageService imageStorageService;

    @BeforeEach
    void setUp() {
        imageStorageService = new ImageStorageService(uploadDirectory, DataSize.ofMegabytes(1));
    }

    @Test
    void storesValidImagesWithUniqueGeneratedNames() throws Exception {
        byte[] imageBytes = createPng();
        MockMultipartFile image = new MockMultipartFile(
                "profileImage", "same-name.png", "image/png", imageBytes);

        String firstName = imageStorageService.store(image);
        String secondName = imageStorageService.store(image);

        assertNotEquals(firstName, secondName);
        assertTrue(firstName.matches("[0-9a-f-]{36}\\.png"));
        assertTrue(Files.exists(uploadDirectory.resolve(firstName)));
        assertTrue(Files.exists(uploadDirectory.resolve(secondName)));
    }

    @Test
    void rejectsContentThatDoesNotMatchTheDeclaredImageType() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "profileImage", "image.png", "image/png", "not an image".getBytes());

        assertThrows(InvalidImageException.class, () -> imageStorageService.store(image));
    }

    @Test
    void enforcesConfiguredMaximumImageSize() {
        MockMultipartFile image = new MockMultipartFile(
                "profileImage", "image.png", "image/png", new byte[128]);
        imageStorageService = new ImageStorageService(uploadDirectory, DataSize.ofBytes(64));

        assertThrows(InvalidImageException.class, () -> imageStorageService.store(image));
    }

    @Test
    void springCanConstructTheImageStorageBean() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.getEnvironment().getPropertySources().addFirst(
                new MapPropertySource("test", Map.of("spring.servlet.multipart.max-file-size", "10MB")));
        context.addBeanFactoryPostProcessor(new PropertySourcesPlaceholderConfigurer());
        context.register(ImageStorageService.class);

        try {
            context.refresh();
            assertNotNull(context.getBean(ImageStorageService.class));
        } finally {
            context.close();
        }
    }

    private byte[] createPng() throws Exception {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }
}
