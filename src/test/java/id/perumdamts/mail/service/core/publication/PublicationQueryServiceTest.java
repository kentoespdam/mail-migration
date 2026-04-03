package id.perumdamts.mail.service.core.publication;

import id.perumdamts.mail.config.StorageProperties;
import id.perumdamts.mail.dto.core.publication.PublicationParams;
import id.perumdamts.mail.dto.core.publication.PublicationResponse;
import id.perumdamts.mail.repository.core.jpa.PublicationRepository;
import id.perumdamts.mail.repository.core.jooq.PublicationQueryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Slf4j
class PublicationQueryServiceTest {

    @Mock
    private PublicationQueryRepository queryRepository;

    @Mock
    private PublicationRepository publicationRepository;

    private PublicationQueryService service;

    @BeforeEach
    void setUp() {
        service = new PublicationQueryService(queryRepository, publicationRepository,
                new StorageProperties("/tmp/test-storage"));
    }

    @Test
    void findAll_shouldDelegateToRepository() {
        PublicationParams params = new PublicationParams();
        PublicationResponse first = publicationResponse("pub-1");
        PublicationResponse second = publicationResponse("pub-2");
        Page<PublicationResponse> page = new PageImpl<>(List.of(first, second), PageRequest.of(0, 10), 12);

        when(queryRepository.findAll(any(PublicationParams.class))).thenReturn(page);

        Page<PublicationResponse> result = service.findAll(params);

        assertThat(result.getContent()).containsExactly(first, second);
        assertThat(result.getTotalElements()).isEqualTo(12);
        verify(queryRepository).findAll(eq(params));
        verifyNoMoreInteractions(queryRepository);

        log.info("paged, {}", result);
    }

    @Test
    void findAll_shouldReturnEmptyPageWhenRepositoryReturnsEmpty() {
        PublicationParams params = new PublicationParams();
        Page<PublicationResponse> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        when(queryRepository.findAll(any(PublicationParams.class))).thenReturn(emptyPage);

        Page<PublicationResponse> result = service.findAll(params);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        verify(queryRepository).findAll(eq(params));
        verifyNoMoreInteractions(queryRepository);

        log.info("empty paged, {}", result);
    }

    @Test
    void findById_shouldReturnPublicationWhenExists() {
        Long id = 10L;
        PublicationResponse expected = publicationResponse("pub-10");

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
        verifyNoMoreInteractions(queryRepository);
    }

    private static PublicationResponse publicationResponse(String id) {
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
                null
        );
    }
}
