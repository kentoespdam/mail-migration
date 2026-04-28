package id.perumdamts.mail.service.core.attachment;

import id.perumdamts.mail.config.StorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AttachmentFileStorageServiceTest {

    @TempDir
    Path tempDir;

    private AttachmentFileStorageService service;

    @BeforeEach
    void setUp() {
        StorageProperties properties = new StorageProperties(tempDir.toString());
        service = new AttachmentFileStorageService(properties);
    }

    @Test
    void store_shouldSaveFileWithNormalizedName() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test file.pdf", "application/pdf", "test content".getBytes());

        var result = service.store(file);

        assertThat(result.originalFileName()).isEqualTo("test file.pdf");
        assertThat(result.systemFileName()).isEqualTo("test_file.pdf");
        assertThat(result.fileSize()).isEqualTo(file.getSize());

        Path storedFile = tempDir.resolve("mail")
                .resolve(LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM")))
                .resolve("test_file.pdf");
        assertThat(Files.exists(storedFile)).isTrue();
        assertThat(Files.readAllBytes(storedFile)).isEqualTo(file.getBytes());
    }

    @Test
    void store_shouldHandleCollisions() {
        String fileName = "test.pdf";
        MockMultipartFile file1 = new MockMultipartFile("file", fileName, "application/pdf", "content 1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file", fileName, "application/pdf", "content 2".getBytes());
        MockMultipartFile file3 = new MockMultipartFile("file", fileName, "application/pdf", "content 3".getBytes());

        var res1 = service.store(file1);
        var res2 = service.store(file2);
        var res3 = service.store(file3);

        assertThat(res1.systemFileName()).isEqualTo("test.pdf");
        assertThat(res2.systemFileName()).isEqualTo("test_1.pdf");
        assertThat(res3.systemFileName()).isEqualTo("test_2.pdf");
    }

    @Test
    void load_shouldReturnResourceWhenFileExists() throws IOException {
        String systemName = "test-file.pdf";
        LocalDateTime now = LocalDateTime.now();
        String monthDir = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"));
        Path mailDir = tempDir.resolve("mail").resolve(monthDir);
        Files.createDirectories(mailDir);
        Files.writeString(mailDir.resolve(systemName), "content");

        Resource resource = service.load(systemName, now);

        assertThat(resource.exists()).isTrue();
        assertThat(resource.getFilename()).isEqualTo(systemName);
    }

    @Test
    void delete_shouldRemoveFileIfExists() throws IOException {
        String systemName = "to-delete.pdf";
        LocalDateTime now = LocalDateTime.now();
        String monthDir = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"));
        Path mailDir = tempDir.resolve("mail").resolve(monthDir);
        Files.createDirectories(mailDir);
        Path file = mailDir.resolve(systemName);
        Files.writeString(file, "to be deleted");

        service.delete(systemName, now);

        assertThat(Files.exists(file)).isFalse();
    }
}
