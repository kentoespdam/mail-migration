package id.perumdamts.mail.service.core.attachment;

import id.perumdamts.mail.config.StorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class AttachmentFileStorageService {

    private static final DateTimeFormatter MONTH_DIR = DateTimeFormatter.ofPattern("yyyyMM");
    private final Path storagePath;

    public AttachmentFileStorageService(StorageProperties storageProperties) {
        String basePath = storageProperties.basePath();
        Path path = Paths.get(basePath);
        if (!path.isAbsolute()) {
            log.warn("Storage base path '{}' is relative. Resolving against user.dir: {}", basePath, System.getProperty("user.dir"));
            path = path.toAbsolutePath();
        }
        this.storagePath = path.resolve("mail").normalize();
        validateStorage();
    }

    private void validateStorage() {
        try {
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
                log.info("Created attachment storage directory: {}", storagePath);
            }
            if (!Files.isWritable(storagePath)) {
                log.error("Attachment storage directory is not writable: {}", storagePath);
            }
            log.info("Attachment storage initialized at: {}", storagePath);
        } catch (IOException e) {
            log.error("Failed to initialize attachment storage directory: {}", storagePath, e);
        }
    }

    public record StoredFile(String systemFileName, String originalFileName, long fileSize) {
    }

    public StoredFile store(MultipartFile file) {
        String monthDir = LocalDateTime.now().format(MONTH_DIR);
        Path dir = storagePath.resolve(monthDir);

        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create directory: " + dir, e);
        }

        String originalFilename = file.getOriginalFilename();
        String basename = getBasename(originalFilename);
        int dot = basename.lastIndexOf('.');
        String stem = (dot >= 0) ? basename.substring(0, dot) : basename;
        String ext = (dot >= 0) ? basename.substring(dot + 1).toLowerCase() : "";

        String normalizedStem = normalizeStem(stem);
        String finalExt = ext.isEmpty() ? "" : "." + ext;

        String systemFileName = normalizedStem + finalExt;
        Path target = dir.resolve(systemFileName).normalize();

        int counter = 1;
        while (counter <= 10000) {
            if (!target.startsWith(storagePath)) {
                throw new IllegalArgumentException("Invalid file path");
            }

            try {
                try (var is = file.getInputStream();
                     var os = Files.newOutputStream(target, StandardOpenOption.CREATE_NEW)) {
                    is.transferTo(os);
                }
                return new StoredFile(target.getFileName().toString(), originalFilename, file.getSize());
            } catch (FileAlreadyExistsException e) {
                systemFileName = normalizedStem + "_" + counter + finalExt;
                target = dir.resolve(systemFileName).normalize();
                counter++;
            } catch (IOException e) {
                throw new IllegalStateException("Failed to store file: " + originalFilename, e);
            }
        }

        throw new IllegalStateException("Could not generate a unique file name after 10000 attempts for: " + originalFilename);
    }

    private String normalizeStem(String stem) {
        if (stem == null || stem.isBlank()) {
            return "file";
        }
        // Replace whitespace with underscore
        String normalized = stem.replaceAll("\\s+", "_")
                .replaceAll("[\\p{Cntrl}]", "")
                .trim();

        if (normalized.isEmpty()) {
            return "file";
        }
        return normalized;
    }

    public void delete(String systemFileName, LocalDateTime createdAt) {
        if (systemFileName == null || systemFileName.isBlank()) {
            return;
        }

        Path file = findFile(systemFileName, createdAt);
        if (file != null) {
            try {
                Files.deleteIfExists(file);
                log.info("Deleted attachment file: {}", file);
            } catch (IOException e) {
                log.warn("Failed to delete attachment file: {}", file, e);
            }
        }
    }

    public Resource load(String systemFileName, LocalDateTime createdAt) {
        Path file = findFile(systemFileName, createdAt);
        if (file == null) {
            throw new IllegalStateException("Attachment file not found on disk: " + systemFileName);
        }

        try {
            return new UrlResource(file.toUri());
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Malformed attachment file path: " + file, e);
        }
    }

    private Path findFile(String systemFileName, LocalDateTime createdAt) {
        if (systemFileName == null || systemFileName.isBlank()) {
            return null;
        }

        String basename = getBasename(systemFileName);
        if (basename.isEmpty()) {
            log.warn("Normalized systemFileName is empty: {}", systemFileName);
            return null;
        }

        // 1. Try canonical path: mail/{yyyyMM}/{systemFileName}
        if (createdAt != null) {
            String monthDir = createdAt.format(MONTH_DIR);
            Path path = storagePath.resolve(monthDir).resolve(basename).normalize();
            if (path.startsWith(storagePath) && Files.exists(path)) {
                return path;
            }
        }

        // 2. Try flat path: mail/{systemFileName}
        Path flatPath = storagePath.resolve(basename).normalize();
        if (flatPath.startsWith(storagePath) && Files.exists(flatPath)) {
            log.warn("Attachment file {} found in flat path (legacy)", basename);
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
                log.warn("Attachment file {} found in non-canonical path: {}", basename, path);
                return path;
            }
        } catch (IOException e) {
            log.error("Error searching for attachment file: {}", basename, e);
        }

        return null;
    }

    private String getBasename(String path) {
        if (path == null) return "";
        String normalized = path.replace('\\', '/');
        int lastSlash = normalized.lastIndexOf('/');
        String basename = lastSlash >= 0 ? normalized.substring(lastSlash + 1) : normalized;
        return basename.trim();
    }
}
