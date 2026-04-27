package id.perumdamts.mail.service.core.publication;

import id.perumdamts.mail.config.StorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class PublicationFileStorageService {

    private static final DateTimeFormatter MONTH_DIR = DateTimeFormatter.ofPattern("yyyyMM");
    private final Path storagePath;

    public PublicationFileStorageService(StorageProperties storageProperties) {
        String basePath = storageProperties.basePath();
        Path path = Paths.get(basePath);
        if (!path.isAbsolute()) {
            log.warn("Storage base path '{}' is relative. Resolving against user.dir: {}", basePath, System.getProperty("user.dir"));
            path = path.toAbsolutePath();
        }
        this.storagePath = path.resolve("publik").normalize();
        validateStorage();
    }

    private void validateStorage() {
        try {
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
                log.info("Created storage directory: {}", storagePath);
            }
            if (!Files.isWritable(storagePath)) {
                log.error("Storage directory is not writable: {}", storagePath);
            }
            log.info("Publication storage initialized at: {}", storagePath);
        } catch (IOException e) {
            log.error("Failed to initialize storage directory: {}", storagePath, e);
        }
    }

    public record StoredFile(String systemFileName, String originalFileName, long fileSize) {
    }

    public StoredFile store(MultipartFile file) {
        String monthDir = LocalDateTime.now().format(MONTH_DIR);
        String ext = extractExtension(file.getOriginalFilename());
        String systemFileName = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
        Path dir = storagePath.resolve(monthDir);

        try {
            Files.createDirectories(dir);
            Path target = dir.resolve(systemFileName).normalize();
            if (!target.startsWith(storagePath)) {
                throw new IllegalArgumentException("Invalid file path");
            }
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return new StoredFile(systemFileName, file.getOriginalFilename(), file.getSize());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file: " + file.getOriginalFilename(), e);
        }
    }

    public void delete(String systemFileName, LocalDateTime createdAt) {
        if (systemFileName == null || systemFileName.isBlank()) {
            return;
        }

        Path file = findFile(systemFileName, createdAt);
        if (file != null) {
            try {
                Files.deleteIfExists(file);
                log.info("Deleted publication file: {}", file);
            } catch (IOException e) {
                log.warn("Failed to delete file: {}", file, e);
            }
        }
    }

    public Resource load(String systemFileName, LocalDateTime createdAt) {
        Path file = findFile(systemFileName, createdAt);
        if (file == null) {
            throw new IllegalStateException("File not found on disk: " + systemFileName);
        }

        try {
            return new UrlResource(file.toUri());
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Malformed file path: " + file, e);
        }
    }

    private Path findFile(String systemFileName, LocalDateTime createdAt) {
        if (systemFileName == null || systemFileName.isBlank()) {
            return null;
        }

        // Normalize: extract basename only to prevent double path resolution
        String basename = getBasename(systemFileName);
        if (basename.isEmpty()) {
            log.warn("Normalized systemFileName is empty: {}", systemFileName);
            return null;
        }

        // 1. Try canonical path: publik/{yyyyMM}/{systemFileName}
        if (createdAt != null) {
            String monthDir = createdAt.format(MONTH_DIR);
            Path path = storagePath.resolve(monthDir).resolve(basename).normalize();
            if (path.startsWith(storagePath) && Files.exists(path)) {
                return path;
            }
        }

        // 2. Try flat path: publik/{systemFileName}
        Path flatPath = storagePath.resolve(basename).normalize();
        if (flatPath.startsWith(storagePath) && Files.exists(flatPath)) {
            log.warn("File {} found in flat path (legacy)", basename);
            return flatPath;
        }

        // 3. Last resort: search in all yyyyMM subdirectories
        try (var stream = Files.list(storagePath)) {
            var found = stream
                    .filter(Files::isDirectory)
                    .filter(p -> p.getFileName().toString().matches("\\d{6}"))
                    .map(p -> p.resolve(basename))
                    .filter(Files::exists)
                    .findFirst();

            if (found.isPresent()) {
                Path path = found.get().normalize();
                log.warn("File {} found in non-canonical path: {}", basename, path);
                return path;
            }
        } catch (IOException e) {
            log.error("Error searching for file: {}", basename, e);
        }

        return null;
    }

    private String getBasename(String path) {
        if (path == null) return "";
        // Handle both / and \ as separators
        String normalized = path.replace('\\', '/');
        int lastSlash = normalized.lastIndexOf('/');
        String basename = lastSlash >= 0 ? normalized.substring(lastSlash + 1) : normalized;
        return basename.trim();
    }

    private String extractExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1).toLowerCase() : "";
    }
}
