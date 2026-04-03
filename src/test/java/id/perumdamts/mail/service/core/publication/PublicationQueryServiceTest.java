package id.perumdamts.mail.service.core.publication;

import id.perumdamts.mail.config.StorageProperties;
import id.perumdamts.mail.dto.common.FileDownloadResource;
import id.perumdamts.mail.dto.core.publication.PublicationParams;
import id.perumdamts.mail.dto.core.publication.PublicationResponse;
import id.perumdamts.mail.entity.core.Publication;
import id.perumdamts.mail.repository.core.jpa.PublicationRepository;
import id.perumdamts.mail.repository.core.jooq.PublicationQueryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
class PublicationQueryServiceTest {

    @Mock
    private PublicationQueryRepository queryRepository;

    @Mock
    private PublicationRepository publicationRepository;

    @Mock
    private StorageProperties storageProperties;

    @InjectMocks
    private PublicationQueryService service;

    @Test
    void findAll_shouldReturnPageUsingFirstItemTotalCount() {
        PublicationParams params = new PublicationParams();
        PublicationResponse first = publicationResponse("pub-1", 2);
        PublicationResponse second = publicationResponse("pub-2", 2);
        List<PublicationResponse> items = List.of(first, second);

        when(queryRepository.findAll(any(PublicationParams.class))).thenReturn(items);

        Page<PublicationResponse> result = service.findAll(params);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getTotalCount()).isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNumber()).isEqualTo(params.getPage());
        assertThat(result.getSize()).isEqualTo(params.getSize());
        verify(queryRepository).findAll(eq(params));
        verifyNoMoreInteractions(queryRepository);
    }

    @Test
    void findAll_shouldReturnEmptyPageWhenRepositoryReturnsEmptyList() {
        PublicationParams params = new PublicationParams();

        when(queryRepository.findAll(any(PublicationParams.class))).thenReturn(List.of());

        Page<PublicationResponse> result = service.findAll(params);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        verify(queryRepository).findAll(eq(params));
        verifyNoMoreInteractions(queryRepository);
    }

    @Test
    void findById_shouldReturnPublicationWhenExists() {
        Long id = 10L;
        PublicationResponse expected = publicationResponse("pub-10", null);

        when(queryRepository.findById(any(Long.class))).thenReturn(Optional.of(expected));

        PublicationResponse result = service.findById(id);

        assertThat(result).isEqualTo(expected);
        verify(queryRepository).findById(eq(id));
        verifyNoMoreInteractions(queryRepository);
    }

    @Test
    void findById_shouldThrowEntityNotFoundWhenMissing() {
        Long id = 99L;
        when(queryRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Publication not found: 99");
        verify(queryRepository).findById(eq(id));
    }

    @Test
    void downloadFile_shouldReturnResourceWhenFileExists() throws IOException {
        Long id = 10L;
        Publication pub = new Publication();
        pub.setFileName("test.pdf");
        pub.setFilePath("publik/test.pdf");

        Path tempDir = Files.createTempDirectory("mail-test");
        Path filePath = tempDir.resolve("publik/test.pdf");
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, "test-content");

        when(publicationRepository.findById(eq(id))).thenReturn(Optional.of(pub));
        when(storageProperties.basePath()).thenReturn(tempDir.toString());

        FileDownloadResource result = service.downloadFile(id);

        assertThat(result.fileName()).isEqualTo("test.pdf");
        assertThat(result.resource()).isNotNull();
        assertThat(result.resource().exists()).isTrue();
        assertThat(result.contentType()).isNotBlank();

        // Clean up
        Files.deleteIfExists(filePath);
        Files.deleteIfExists(filePath.getParent());
        Files.deleteIfExists(tempDir);
    }

    @Test
    void downloadFile_shouldRejectPathTraversal() {
        Long id = 10L;
        Publication pub = new Publication();
        pub.setFileName("evil.pdf");
        pub.setFilePath("../../etc/passwd");

        Path tempDir = Path.of("/tmp/mail-test-traversal");

        when(publicationRepository.findById(eq(id))).thenReturn(Optional.of(pub));
        when(storageProperties.basePath()).thenReturn(tempDir.toString());

        assertThatThrownBy(() -> service.downloadFile(id))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Invalid file path");
    }

    private static PublicationResponse publicationResponse(String id, Integer totalCount) {
        return new PublicationResponse(
                id,
                "title",
                "description",
                null,
                "PUBLISHED",
                null,
                "file.pdf",
                123,
                "Creator",
                "Manager",
                1,
                null,
                null,
                totalCount
        );
    }
}
