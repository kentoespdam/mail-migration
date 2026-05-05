package id.perumdamts.mail.service.core.mail;

import id.perumdamts.mail.entity.core.Mail;
import id.perumdamts.mail.entity.core.MailRecipient;
import id.perumdamts.mail.event.MailSentEvent;
import id.perumdamts.mail.repository.core.jpa.MailRecipientRepository;
import id.perumdamts.mail.repository.core.jpa.MailRepository;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.service.core.mail.numbering.MailNumberGenerator;
import id.perumdamts.mail.service.core.usertask.UserTaskCommandService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service untuk pengiriman surat dengan 10 side-effects.
 * Implementasi dari send() di source PHP (mailmodel.php:812-1016).
 * Migration notes:
 * - Core transaction (@Transactional) + event-driven side effects
 * (@Async, @EventListener)
 * - Email notification → MailNotificationService (async)
 * - Statistik update → MailStatisticService (async)
 * - Response time tracking → MailResponseTimeService (async)
 * - Inbox creation bisa batch INSERT untuk performance
 */
@Service
@RequiredArgsConstructor
public class MailSendService {

    private static final Logger log = LoggerFactory.getLogger(MailSendService.class);

    private final MailRepository mailRepository;
    private final MailRecipientRepository recipientRepository;
    private final UserTaskCommandService userTaskCommandService;
    private final MailNumberGenerator mailNumberGenerator;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Send mail dengan 10 side-effects dalam satu transaksi.
     * Side-effects:
     * 1. Validasi recipient
     * 2. Generate nomor surat
     * 3. Update status mail (DRAFT → SENT)
     * 4. Create inbox per recipient
     * 5. Kirim email notification (async)
     * 6. Update statistik kategori dan organisasi (async)
     * 7. Move draft ke sent
     * 8. Mark parent sebagai read (jika reply)
     * 9. Track response time (async)
     * 10. Build toStr (recipient list)
     *
     * @param mailId    ID mail yang akan dikirim
     * @param principal user yang mengirim
     * @return mail yang sudah dikirim
     */
    @Transactional
    public Mail send(Long mailId, MailPrincipal principal) {
        // Get mail dan validate
        Mail mail = getMailOrThrow(mailId);
        if (!mail.isDraft()) {
            throw new IllegalStateException("Mail already sent: " + mailId);
        }

        // Side-effect 1: Validasi recipient
        List<MailRecipient> recipients = recipientRepository.findByMailId(mailId);
        if (recipients.isEmpty()) {
            throw new IllegalArgumentException("Cannot send mail without recipients");
        }

        // Side-effect 2: Generate nomor surat
        String mailNumber = mailNumberGenerator.generate(mail);

        // Side-effect 3: Update status mail
        mail.send(mailNumber);

        // Side-effect 10: Build toStr
        String toStr = buildToStr(recipients);
        mail.setToStr(toStr);

        // Save mail dengan status SENT
        mailRepository.save(mail);

        // Side-effect 4: Create inbox per recipient (batch INSERT)
        Long senderId = Long.parseLong(principal.userId());
        List<Long> recipientUserIds = recipients.stream()
                .map(MailRecipient::getUserId)
                .toList();
        userTaskCommandService.createInboxes(mailId, recipientUserIds);

        // Side-effect 7: Move sender's task dari DRAFT ke SENT
        userTaskCommandService.moveFromDraftToSent(senderId, mailId);

        // Side-effect 8: Jika reply, mark parent sebagai read untuk sender
        if (mail.getParentMail() != null) {
            userTaskCommandService.markParentAsRead(senderId, mail.getParentMail().getId());
        }

        // Side-effect 5, 6, 9: Publish event untuk async processing
        eventPublisher.publishEvent(new MailSentEvent(
                mailId, senderId, principal.name(), recipientUserIds));

        log.info("Mail sent successfully: mailId={}, mailNumber={}, sender={}, recipients={}",
                mailId, mailNumber, principal.name(), recipientUserIds.size());

        return mail;
    }

    /**
     * Build recipient list string untuk toStr field.
     */
    private String buildToStr(List<MailRecipient> recipients) {
        return Mail.buildToStr(recipients);
    }

    private Mail getMailOrThrow(Long mailId) {
        return mailRepository.findByIdWithDetails(mailId)
                .orElseThrow(() -> new IllegalArgumentException("Mail not found: " + mailId));
    }
}
