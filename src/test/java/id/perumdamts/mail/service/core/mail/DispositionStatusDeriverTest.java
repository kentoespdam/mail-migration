package id.perumdamts.mail.service.core.mail;

import id.perumdamts.mail.entity.core.Mail;
import id.perumdamts.mail.enums.DispositionStatus;
import id.perumdamts.mail.repository.core.jpa.MailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DispositionStatusDeriverTest {

    @Mock
    private MailRepository mailRepository;

    private DispositionStatusDeriver service;

    @BeforeEach
    void setUp() {
        service = new DispositionStatusDeriver(mailRepository);
    }

    @Test
    void deriveStatus_rootWithoutChild_returnsPending() {
        Mail root = createMail(1L, null, null, null);
        when(mailRepository.findById(1L)).thenReturn(Optional.of(root));
        when(mailRepository.findByRootIdAndDeletedFalse(1L)).thenReturn(List.of());

        DispositionStatusDeriver.DispositionStatusResult result = service.deriveStatus(1L);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(DispositionStatus.PENDING);
    }

    @Test
    void deriveStatus_rootWithNonDeletedChild_returnsInProgress() {
        Mail root = createMail(1L, null, null, null);
        Mail child = createMail(2L, 1L, root, null);

        when(mailRepository.findById(1L)).thenReturn(Optional.of(root));
        when(mailRepository.findByRootIdAndDeletedFalse(1L)).thenReturn(List.of(child));

        DispositionStatusDeriver.DispositionStatusResult result = service.deriveStatus(1L);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(DispositionStatus.IN_PROGRESS);
    }

    @Test
    void deriveStatus_rootWithRepliedChild_returnsDone() {
        Mail root = createMail(1L, null, null, null);
        Mail child = createMail(2L, 1L, root, null);

        when(mailRepository.findById(1L)).thenReturn(Optional.of(root));
        when(mailRepository.findByRootIdAndDeletedFalse(1L)).thenReturn(List.of(child));

        DispositionStatusDeriver.DispositionStatusResult result = service.deriveStatus(1L);

        assertThat(result.status()).isIn(DispositionStatus.IN_PROGRESS, DispositionStatus.DONE);
    }

    @Test
    void deriveStatus_deletedMail_returnsNull() {
        Mail deleted = createMail(1L, null, null, null);
        deleted.setDeleted(true);

        when(mailRepository.findById(1L)).thenReturn(Optional.of(deleted));

        assertThat(service.deriveStatus(1L)).isNull();
    }

    @Test
    void deriveStatus_notFound_returnsNull() {
        when(mailRepository.findById(999L)).thenReturn(Optional.empty());

        assertThat(service.deriveStatus(999L)).isNull();
    }

    @Test
    void deriveStatus_softDeletedChildrenExcluded() {
        Mail root = createMail(1L, null, null, null);
        Mail deletedChild = createMail(2L, 1L, root, null);
        deletedChild.setDeleted(true);

        when(mailRepository.findById(1L)).thenReturn(Optional.of(root));
        when(mailRepository.findByRootIdAndDeletedFalse(1L)).thenReturn(List.of());

        DispositionStatusDeriver.DispositionStatusResult result = service.deriveStatus(1L);

        assertThat(result.status()).isEqualTo(DispositionStatus.PENDING);
    }

    private Mail createMail(Long id, Long rootId, Mail rootMail, Mail parentMail) {
        Mail mail = new Mail();
        mail.setId(id);
        mail.setDeleted(false);
        mail.setRootMail(rootMail);
        mail.setParentMail(parentMail);
        mail.setSubject("Subject " + id);
        mail.setMaxResponseDate(LocalDate.now().plusDays(7));
        return mail;
    }
}
