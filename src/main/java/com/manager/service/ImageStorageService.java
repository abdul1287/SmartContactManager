package com.manager.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import com.manager.exception.InvalidImageException;

@Service
public class ImageStorageService {

    private final Path uploadDirectory;
    private final long maxImageSizeBytes;

    @Autowired
    public ImageStorageService(
            @Value("${spring.servlet.multipart.max-file-size:10MB}") String maxImageSize) {
        this(Paths.get("uploads"), DataSize.parse(maxImageSize));
    }

    ImageStorageService(Path uploadDirectory, DataSize maxImageSize) {
        this.uploadDirectory = uploadDirectory.toAbsolutePath().normalize();
        this.maxImageSizeBytes = maxImageSize.toBytes();
    }

    public String store(MultipartFile image) throws IOException {
        String extension = validateImage(image);
        Files.createDirectories(uploadDirectory);
        String filename = UUID.randomUUID() + "." + extension;
        Path target = uploadDirectory.resolve(filename);
        try (InputStream input = image.getInputStream()) {
            Files.copy(input, target);
        }
        return filename;
    }

    public void delete(String filename) throws IOException {
        Path target = uploadDirectory.resolve(filename).normalize();
        if (!target.getParent().equals(uploadDirectory)) {
            throw new IllegalArgumentException("Invalid stored image name.");
        }
        Files.deleteIfExists(target);
    }

    private String validateImage(MultipartFile image) throws IOException {
        if (image.getSize() > maxImageSizeBytes) {
            throw new InvalidImageException("Image exceeds the maximum allowed size.");
        }

        String contentType = image.getContentType();
        if (contentType == null
                || !(contentType.equalsIgnoreCase("image/jpeg")
                        || contentType.equalsIgnoreCase("image/png")
                        || contentType.equalsIgnoreCase("image/gif"))) {
            throw new InvalidImageException("Only JPEG, PNG, and GIF images are allowed.");
        }

        try (InputStream input = image.getInputStream();
                ImageInputStream imageInput = ImageIO.createImageInputStream(input)) {
            if (imageInput == null) {
                throw new InvalidImageException("Uploaded file is not a supported image.");
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);
            if (!readers.hasNext()) {
                throw new InvalidImageException("Uploaded file is not a supported image.");
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(imageInput, true, true);
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                return switch (format) {
                    case "jpg", "jpeg" -> {
                        requireContentType(contentType, "image/jpeg");
                        yield "jpg";
                    }
                    case "png" -> {
                        requireContentType(contentType, "image/png");
                        yield "png";
                    }
                    case "gif" -> {
                        requireContentType(contentType, "image/gif");
                        yield "gif";
                    }
                    default -> throw new InvalidImageException("Only JPEG, PNG, and GIF images are allowed.");
                };
            } finally {
                reader.dispose();
            }
        }
    }

    private void requireContentType(String actual, String expected) {
        if (!actual.equalsIgnoreCase(expected)) {
            throw new InvalidImageException("Image content does not match its file type.");
        }
    }
}
