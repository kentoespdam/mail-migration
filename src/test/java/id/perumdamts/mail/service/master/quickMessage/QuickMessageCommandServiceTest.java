package id.perumdamts.mail.service.master.quickMessage;

import id.perumdamts.mail.dto.master.quickMessage.QuickMessageResponse;
import id.perumdamts.mail.entity.master.QuickMessage;
import id.perumdamts.mail.enums.RecordStatus;
import id.perumdamts.mail.repository.master.jooq.QuickMessageQueryRepository;
import id.perumdamts.mail.repository.master.jpa.QuickMessageRepository;
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
        quickMessage.setStatus(RecordStatus.ACTIVE);
        when(repository.findById(id)).thenReturn(Optional.of(quickMessage));
        when(queryRepository.findById(id)).thenReturn(Optional.of(mock(QuickMessageResponse.class)));

        service.toggleStatus(id);

        assertThat(quickMessage.getStatus()).isEqualTo(RecordStatus.INACTIVE);
        verify(repository).save(quickMessage);
    }

    @Test
    void toggleStatus_FromInactiveToActive_ShouldSuccess() {
        quickMessage.setStatus(RecordStatus.INACTIVE);
        when(repository.findById(id)).thenReturn(Optional.of(quickMessage));
        when(queryRepository.findById(id)).thenReturn(Optional.of(mock(QuickMessageResponse.class)));

        service.toggleStatus(id);

        assertThat(quickMessage.getStatus()).isEqualTo(RecordStatus.ACTIVE);
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
        quickMessage.setStatus(RecordStatus.DELETED);
        when(repository.findById(id)).thenReturn(Optional.of(quickMessage));

        assertThatThrownBy(() -> service.toggleStatus(id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot toggle status of a DELETED record");
    }
}
