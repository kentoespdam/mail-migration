package id.perumdamts.mail.service.master.documentType;

import id.perumdamts.mail.dto.master.documentType.DocumentTypeResponse;
import id.perumdamts.mail.entity.master.DocumentType;
import id.perumdamts.mail.enums.RecordStatusActive;
import id.perumdamts.mail.repository.master.jooq.DocumentTypeQueryRepository;
import id.perumdamts.mail.repository.master.jpa.DocumentTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentTypeCommandServiceTest {

    @Mock
    private DocumentTypeRepository repository;
    @Mock
    private DocumentTypeQueryRepository queryRepository;

    @InjectMocks
    private DocumentTypeCommandService service;

    private DocumentType documentType;
    private final Long id = 1L;

    @BeforeEach
    void setUp() {
        documentType = new DocumentType("KTP");
        documentType.setId(id);
    }

    @Test
    void toggleStatus_FromActiveToInactive_ShouldSuccess() {
        documentType.setStatus(RecordStatusActive.ACTIVE);
        when(repository.findById(id)).thenReturn(Optional.of(documentType));
        when(queryRepository.findById(id)).thenReturn(Optional.of(mock(DocumentTypeResponse.class)));

        service.toggleStatus(id);

        assertThat(documentType.getStatus()).isEqualTo(RecordStatusActive.INACTIVE);
        verify(repository).save(documentType);
    }

    @Test
    void toggleStatus_FromInactiveToActive_ShouldSuccess() {
        documentType.setStatus(RecordStatusActive.INACTIVE);
        when(repository.findById(id)).thenReturn(Optional.of(documentType));
        when(queryRepository.findById(id)).thenReturn(Optional.of(mock(DocumentTypeResponse.class)));

        service.toggleStatus(id);

        assertThat(documentType.getStatus()).isEqualTo(RecordStatusActive.ACTIVE);
        verify(repository).save(documentType);
    }

    @Test
    void toggleStatus_NotFound_ShouldThrowException() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.toggleStatus(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void toggleStatus_DeletedRecord_ShouldThrowException() {
        documentType.setStatus(RecordStatusActive.INACTIVE);
        documentType.setDeleted(Boolean.TRUE);
        when(repository.findById(id)).thenReturn(Optional.of(documentType));

        assertThatThrownBy(() -> service.toggleStatus(id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot toggle status of a deleted record");
    }
}
