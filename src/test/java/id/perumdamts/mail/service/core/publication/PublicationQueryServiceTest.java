package id.perumdamts.mail.service.core.publication;

import id.perumdamts.mail.dto.common.PagedResponse;
import id.perumdamts.mail.dto.core.publication.PublicationDto;
import id.perumdamts.mail.dto.core.publication.PublicationParams;
import id.perumdamts.mail.repository.core.jooq.PublicationQueryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @InjectMocks
    private PublicationQueryService service;

    @Test
    void list_shouldBuildPagedResponseUsingFirstItemTotalCount() {
        PublicationParams params = new PublicationParams();
        PublicationDto first = publicationDto("pub-1", 12);
        PublicationDto second = publicationDto("pub-2", 12);
        List<PublicationDto> items = List.of(first, second);

        when(queryRepository.findAll(any(PublicationParams.class))).thenReturn(items);

        PagedResponse<PublicationDto> result = service.list(params);

        assertThat(result.content()).containsExactly(first, second);
        assertThat(result.totalElements()).isEqualTo(12);
        assertThat(result.page()).isEqualTo(params.getPage());
        assertThat(result.size()).isEqualTo(params.getSize());
        verify(queryRepository).findAll(eq(params));
        verifyNoMoreInteractions(queryRepository);

        log.info("paged, {}",result);
    }

    @Test
    void list_shouldReturnZeroTotalWhenRepositoryReturnsEmptyList() {
        PublicationParams params = new PublicationParams();

        when(queryRepository.findAll(any(PublicationParams.class))).thenReturn(List.of());

        PagedResponse<PublicationDto> result = service.list(params);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
        assertThat(result.page()).isEqualTo(params.getPage());
        assertThat(result.size()).isEqualTo(params.getSize());
        verify(queryRepository).findAll(eq(params));
        verifyNoMoreInteractions(queryRepository);

        log.info("empty paged, {}",result);
    }

    @Test
    void findById_shouldReturnPublicationWhenExists() {
        Long id = 10L;
        PublicationDto expected = publicationDto("pub-10", null);

        when(queryRepository.findById(any(Long.class))).thenReturn(Optional.of(expected));

        PublicationDto result = service.findById(id);

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

    private static PublicationDto publicationDto(String id, Integer totalCount) {
        return new PublicationDto(
                id,
                "title",
                "description",
                null,
                "PUBLISHED",
                null,
                "file.pdf",
                "/tmp/file.pdf",
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
