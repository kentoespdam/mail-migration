package id.perumdamts.mail.service.master.quickMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import id.perumdamts.mail.dto.master.quickMessage.QuickMessageResponse;
import id.perumdamts.mail.entity.master.QuickMessage;
import id.perumdamts.mail.enums.RecordStatusActive;
import id.perumdamts.mail.repository.master.jooq.QuickMessageQueryRepository;
import id.perumdamts.mail.repository.master.jpa.QuickMessageRepository;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class QuickMessageCommandServiceTest {

    @Mock
    private QuickMessageRepository repository;
    @Mock
    private QuickMessageQueryRepository queryRepository;

    @InjectMocks
    private QuickMessageCommandService service;

    private QuickMessage quickMessage;
    private final Long id = 1L;

    @BeforeEach
    void setUp() {
        quickMessage = new QuickMessage("Sample message");
        quickMessage.setId(id);
    }

    @Test
    void toggleStatus_FromActiveToInactive_ShouldSuccess() {
        quickMessage.setStatus(RecordStatusActive.ACTIVE);
        when(repository.findById(id)).thenReturn(Optional.of(quickMessage));
        when(queryRepository.findById(id)).thenReturn(Optional.of(mock(QuickMessageResponse.class)));

        service.toggleStatus(id);

        assertThat(quickMessage.getStatus()).isEqualTo(RecordStatusActive.INACTIVE);
        verify(repository).save(quickMessage);
    }

    @Test
    void toggleStatus_FromInactiveToActive_ShouldSuccess() {
        quickMessage.setStatus(RecordStatusActive.INACTIVE);
        when(repository.findById(id)).thenReturn(Optional.of(quickMessage));
        when(queryRepository.findById(id)).thenReturn(Optional.of(mock(QuickMessageResponse.class)));

        service.toggleStatus(id);

        assertThat(quickMessage.getStatus()).isEqualTo(RecordStatusActive.ACTIVE);
        verify(repository).save(quickMessage);
    }

    @Test
    void toggleStatus_NotFound_ShouldThrowException() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.toggleStatus(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void toggleStatus_DeletedRecord_ShouldThrowException() {
        quickMessage.setStatus(RecordStatusActive.INACTIVE);
        quickMessage.setDeleted(Boolean.TRUE);
        when(repository.findById(id)).thenReturn(Optional.of(quickMessage));

        assertThatThrownBy(() -> service.toggleStatus(id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot toggle status of a deleted record");
    }
}
