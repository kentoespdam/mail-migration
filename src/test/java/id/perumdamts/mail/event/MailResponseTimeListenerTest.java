package id.perumdamts.mail.event;

import id.perumdamts.mail.entity.core.Mail;
import id.perumdamts.mail.entity.core.MailResponseTime;
import id.perumdamts.mail.entity.master.MailCategory;
import id.perumdamts.mail.entity.master.MailType;
import id.perumdamts.mail.repository.core.jpa.MailRepository;
import id.perumdamts.mail.repository.core.jpa.MailResponseTimeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailResponseTimeListenerTest {

    @Mock
    private MailResponseTimeRepository responseTimeRepository;

    @Mock
    private MailRepository mailRepository;

    private MailResponseTimeListener listener;

    @BeforeEach
    void setUp() {
        listener = new MailResponseTimeListener(responseTimeRepository, mailRepository);
    }

    @Test
    void onMailSent_replyToParent_shouldRecordResponseTime() {
        LocalDateTime parentCreated = LocalDateTime.of(2026, 5, 1, 10, 0);
        LocalDateTime replyCreated = LocalDateTime.of(2026, 5, 1, 12, 0);

        Mail parentMail = new Mail();
        parentMail.setId(1L);
        parentMail.setCreatedDate(parentCreated);
        MailType mailType = new MailType();
        mailType.setId(1L);
        parentMail.setMailType(mailType);
        MailCategory mailCategory = new MailCategory();
        mailCategory.setId(1L);
        parentMail.setMailCategory(mailCategory);

        Mail replyMail = new Mail();
        replyMail.setId(2L);
        replyMail.setCreatedDate(replyCreated);
        replyMail.setMailType(mailType);
        replyMail.setMailCategory(mailCategory);
        replyMail.setParentMail(parentMail);

        MailSentEvent event = new MailSentEvent(2L, 100L, "Sender", List.of(200L));

        when(mailRepository.findById(2L)).thenReturn(Optional.of(replyMail));
        when(mailRepository.findById(1L)).thenReturn(Optional.of(parentMail));
        when(responseTimeRepository.findByOriginalMailId(1L)).thenReturn(Optional.empty());

        listener.onMailSent(event);

        ArgumentCaptor<MailResponseTime> captor = ArgumentCaptor.forClass(MailResponseTime.class);
        verify(responseTimeRepository).save(captor.capture());

        MailResponseTime saved = captor.getValue();
        assertThat(saved.getOriginalMail().getId()).isEqualTo(1L);
        assertThat(saved.getReplyMail().getId()).isEqualTo(2L);
        assertThat(saved.getOriginalDate()).isEqualTo(parentCreated);
        assertThat(saved.getReplyDate()).isEqualTo(replyCreated);
        assertThat(saved.getResponseTime()).isEqualTo(7200);
    }

    @Test
    void onMailSent_noParent_shouldSkip() {
        Mail rootMail = new Mail();
        rootMail.setId(1L);
        rootMail.setCreatedDate(LocalDateTime.now());
        rootMail.setParentMail(null);

        MailSentEvent event = new MailSentEvent(1L, 100L, "Sender", List.of(200L));

        when(mailRepository.findById(1L)).thenReturn(Optional.of(rootMail));

        listener.onMailSent(event);

        verify(responseTimeRepository, never()).save(any());
    }

    @Test
    void onMailSent_existingRecord_shouldSkip() {
        Mail parentMail = new Mail();
        parentMail.setId(1L);
        parentMail.setCreatedDate(LocalDateTime.of(2026, 5, 1, 10, 0));

        Mail replyMail = new Mail();
        replyMail.setId(2L);
        replyMail.setCreatedDate(LocalDateTime.of(2026, 5, 1, 12, 0));
        replyMail.setParentMail(parentMail);

        MailSentEvent event = new MailSentEvent(2L, 100L, "Sender", List.of(200L));

        when(mailRepository.findById(2L)).thenReturn(Optional.of(replyMail));
        when(mailRepository.findById(1L)).thenReturn(Optional.of(parentMail));
        when(responseTimeRepository.findByOriginalMailId(1L)).thenReturn(Optional.of(new MailResponseTime()));

        listener.onMailSent(event);

        verify(responseTimeRepository, never()).save(any());
    }

    @Test
    void onMailSent_replyMailNotFound_shouldSkip() {
        MailSentEvent event = new MailSentEvent(99L, 100L, "Sender", List.of(200L));

        when(mailRepository.findById(99L)).thenReturn(Optional.empty());

        listener.onMailSent(event);

        verify(responseTimeRepository, never()).save(any());
    }

    @Test
    void onMailSent_negativeResponseTime_shouldSkip() {
        LocalDateTime parentCreated = LocalDateTime.of(2026, 5, 2, 10, 0);
        LocalDateTime replyCreated = LocalDateTime.of(2026, 5, 1, 12, 0);

        Mail parentMail = new Mail();
        parentMail.setId(1L);
        parentMail.setCreatedDate(parentCreated);

        Mail replyMail = new Mail();
        replyMail.setId(2L);
        replyMail.setCreatedDate(replyCreated);
        replyMail.setParentMail(parentMail);

        MailSentEvent event = new MailSentEvent(2L, 100L, "Sender", List.of(200L));

        when(mailRepository.findById(2L)).thenReturn(Optional.of(replyMail));
        when(mailRepository.findById(1L)).thenReturn(Optional.of(parentMail));
        when(responseTimeRepository.findByOriginalMailId(1L)).thenReturn(Optional.empty());

        listener.onMailSent(event);

        verify(responseTimeRepository, never()).save(any());
    }
}