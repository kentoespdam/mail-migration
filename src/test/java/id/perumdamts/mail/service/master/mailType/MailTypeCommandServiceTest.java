package id.perumdamts.mail.service.master.mailType;

import id.perumdamts.mail.dto.master.mailType.MailTypeResponse;
import id.perumdamts.mail.entity.master.MailType;
import id.perumdamts.mail.enums.RecordStatus;
import id.perumdamts.mail.repository.master.jooq.MailTypeQueryRepository;
import id.perumdamts.mail.repository.master.jpa.MailCategoryRepository;
import id.perumdamts.mail.repository.master.jpa.MailTypeRepository;
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
class MailTypeCommandServiceTest {

    @Mock
    private MailTypeRepository repository;
    @Mock
    private MailCategoryRepository categoryRepository;
    @Mock
    private MailTypeQueryRepository queryRepository;

    @InjectMocks
    private MailTypeCommandService service;

    private MailType mailType;
    private final Long id = 1L;

    @BeforeEach
    void setUp() {
        mailType = new MailType("General");
        mailType.setId(id);
    }

    @Test
    void toggleStatus_FromActiveToInactive_ShouldSuccess() {
        mailType.setStatus(RecordStatus.ACTIVE);
        when(repository.findById(id)).thenReturn(Optional.of(mailType));
        when(queryRepository.findById(id)).thenReturn(Optional.of(mock(MailTypeResponse.class)));

        service.toggleStatus(id);

        assertThat(mailType.getStatus()).isEqualTo(RecordStatus.INACTIVE);
        verify(repository).save(mailType);
    }

    @Test
    void toggleStatus_FromInactiveToActive_ShouldSuccess() {
        mailType.setStatus(RecordStatus.INACTIVE);
        when(repository.findById(id)).thenReturn(Optional.of(mailType));
        when(queryRepository.findById(id)).thenReturn(Optional.of(mock(MailTypeResponse.class)));

        service.toggleStatus(id);

        assertThat(mailType.getStatus()).isEqualTo(RecordStatus.ACTIVE);
        verify(repository).save(mailType);
    }

    @Test
    void toggleStatus_NotFound_ShouldThrowException() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.toggleStatus(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void toggleStatus_DeletedRecord_ShouldThrowException() {
        mailType.setStatus(RecordStatus.DELETED);
        when(repository.findById(id)).thenReturn(Optional.of(mailType));

        assertThatThrownBy(() -> service.toggleStatus(id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot toggle status of a DELETED record");
    }
}
