package id.perumdamts.mail.service.core.publication;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PublicationFileStorageServiceTest {

    @TempDir
    Path tempDir;

    private PublicationFileStorageService service;

    @BeforeEach
    void setUp() {
        StorageProperties properties = new StorageProperties(tempDir.toString());
        service = new PublicationFileStorageService(properties);
    }

    @Test
    void store_shouldSaveFileWithNormalizedName() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test file.pdf", "application/pdf", "test content".getBytes());

        var result = service.store(file);

        assertThat(result.originalFileName()).isEqualTo("test file.pdf");
        assertThat(result.systemFileName()).isEqualTo("test_file.pdf");
        assertThat(result.fileSize()).isEqualTo(file.getSize());

        Path storedFile = tempDir.resolve("publik")
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
    void store_shouldSanitizePathTraversal() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "../../../etc/passwd.txt", "text/plain", "content".getBytes());

        var result = service.store(file);

        assertThat(result.systemFileName()).isEqualTo("passwd.txt");
    }

    @Test
    void store_shouldFallbackWhenStemIsEmpty() {
        MockMultipartFile file = new MockMultipartFile(
                "file", " .pdf", "application/pdf", "content".getBytes());

        var result = service.store(file);

        assertThat(result.systemFileName()).isEqualTo("file.pdf");
    }

    @Test
    void load_shouldReturnResourceWhenFileExists() throws IOException {
        String systemName = "test-file.pdf";
        LocalDateTime now = LocalDateTime.now();
        String monthDir = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"));
        Path publikDir = tempDir.resolve("publik").resolve(monthDir);
        Files.createDirectories(publikDir);
        Files.writeString(publikDir.resolve(systemName), "content");

        Resource resource = service.load(systemName, now);

        assertThat(resource.exists()).isTrue();
        assertThat(resource.getFilename()).isEqualTo(systemName);
    }

    @Test
    void load_shouldFallbackToFlatPathForLegacyFiles() throws IOException {
        String systemName = "legacy-file.pdf";
        Path publikDir = tempDir.resolve("publik");
        Files.createDirectories(publikDir);
        Files.writeString(publikDir.resolve(systemName), "legacy content");

        // Use a date that doesn't match the current month to ensure it doesn't find it canonically
        Resource resource = service.load(systemName, LocalDateTime.now().minusMonths(1));

        assertThat(resource.exists()).isTrue();
        assertThat(resource.getFilename()).isEqualTo(systemName);
    }

    @Test
    void load_shouldSearchInAllSubdirectoriesAsLastResort() throws IOException {
        String systemName = "misplaced-file.pdf";
        String actualMonthDir = "202401";
        Path publikDir = tempDir.resolve("publik").resolve(actualMonthDir);
        Files.createDirectories(publikDir);
        Files.writeString(publikDir.resolve(systemName), "misplaced content");

        // Try to load with null createdAt or wrong createdAt
        Resource resource = service.load(systemName, null);

        assertThat(resource.exists()).isTrue();
        assertThat(resource.getFilename()).isEqualTo(systemName);
    }

    @Test
    void load_shouldThrowExceptionWhenFileNotFound() {
        assertThatThrownBy(() -> service.load("non-existent.pdf", LocalDateTime.now()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("File not found on disk");
    }

    @Test
    void delete_shouldRemoveFileIfExists() throws IOException {
        String systemName = "to-delete.pdf";
        LocalDateTime now = LocalDateTime.now();
        String monthDir = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"));
        Path publikDir = tempDir.resolve("publik").resolve(monthDir);
        Files.createDirectories(publikDir);
        Path file = publikDir.resolve(systemName);
        Files.writeString(file, "to be deleted");

        service.delete(systemName, now);

        assertThat(Files.exists(file)).isFalse();
    }

    @Test
    void load_shouldHandleSystemFileNameWithPrefixPath() throws IOException {
        // This simulates the data corruption where system_file_name contains 'publik/202604/filename.jpg'
        String fileName = "test-file.jpg";
        String monthDir = "202604";
        String prefixedSystemName = "publik/" + monthDir + "/" + fileName;
        LocalDateTime createdAt = LocalDateTime.of(2026, 4, 1, 10, 0);

        Path actualDir = tempDir.resolve("publik").resolve(monthDir);
        Files.createDirectories(actualDir);
        Files.writeString(actualDir.resolve(fileName), "dummy content");

        Resource resource = service.load(prefixedSystemName, createdAt);

        assertThat(resource.exists()).isTrue();
        assertThat(resource.getFilename()).isEqualTo(fileName);
    }

    @Test
    void load_shouldHandleWindowsSeparators() throws IOException {
        String fileName = "win-file.jpg";
        String monthDir = "202604";
        String prefixedSystemName = "publik\\" + monthDir + "\\" + fileName;
        LocalDateTime createdAt = LocalDateTime.of(2026, 4, 1, 10, 0);

        Path actualDir = tempDir.resolve("publik").resolve(monthDir);
        Files.createDirectories(actualDir);
        Files.writeString(actualDir.resolve(fileName), "win content");

        Resource resource = service.load(prefixedSystemName, createdAt);

        assertThat(resource.exists()).isTrue();
        assertThat(resource.getFilename()).isEqualTo(fileName);
    }

    @Test
    void load_shouldHandleMixedSeparatorsAndSpaces() throws IOException {
        String fileName = "mixed-file.jpg";
        String prefixedSystemName = " publik/202604\\" + fileName + " ";
        LocalDateTime createdAt = LocalDateTime.of(2026, 4, 1, 10, 0);

        Path actualDir = tempDir.resolve("publik").resolve("202604");
        Files.createDirectories(actualDir);
        Files.writeString(actualDir.resolve(fileName), "mixed content");

        Resource resource = service.load(prefixedSystemName, createdAt);

        assertThat(resource.exists()).isTrue();
        assertThat(resource.getFilename()).isEqualTo(fileName);
    }

    @Test
    void load_shouldSearchInAllSubdirectoriesAsLastResortWithPrefixedName() throws IOException {
        String fileName = "misplaced-prefixed.pdf";
        String actualMonthDir = "202401";
        // prefixed name that doesn't match the actual month dir
        String prefixedSystemName = "wrong/folder/" + fileName;

        Path actualPath = tempDir.resolve("publik").resolve(actualMonthDir).resolve(fileName);
        Files.createDirectories(actualPath.getParent());
        Files.writeString(actualPath, "misplaced content");

        // createdAt is null or different from actualMonthDir
        Resource resource = service.load(prefixedSystemName, null);

        assertThat(resource.exists()).isTrue();
        assertThat(resource.getFilename()).isEqualTo(fileName);
    }
}
