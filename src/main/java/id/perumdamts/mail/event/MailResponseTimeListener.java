package id.perumdamts.mail.event;

import id.perumdamts.mail.entity.core.Mail;
import id.perumdamts.mail.entity.core.MailResponseTime;
import id.perumdamts.mail.repository.core.jpa.MailRepository;
import id.perumdamts.mail.repository.core.jpa.MailResponseTimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Listener untuk track response time mail secara async.
 * Response time tracking untuk SLA monitoring.
 * Saat mail dikirim (reply), hitung response time terhadap mail original.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MailResponseTimeListener {

    private final MailResponseTimeRepository responseTimeRepository;
    private final MailRepository mailRepository;

    /**
     * Handle MailSentEvent untuk track response time.
     * Dijalankan secara async setelah transaksi commit.
     */
    @TransactionalEventListener
    @Async
    public void onMailSent(MailSentEvent event) {
        log.info("Tracking response time: mailId={}, sender={}",
                event.mailId(), event.senderId());

        Mail replyMail = mailRepository.findById(event.mailId())
                .orElse(null);
        if (replyMail == null) {
            log.warn("Reply mail not found: {}", event.mailId());
            return;
        }

        // Jika reply memiliki parent mail, berarti ini adalah balasan
        Mail parentMail = replyMail.getParentMail();
        if (parentMail == null) {
            log.debug("Mail {} is not a reply, skipping response time tracking", event.mailId());
            return;
        }

        // Ambil ID parent, lalu fetch parent mail secara terpisah untuk menghindari lazy loading
        Long originalMailId = parentMail.getId();
        Mail originalMail = mailRepository.findById(originalMailId)
                .orElse(null);
        if (originalMail == null) {
            log.warn("Original mail not found: {}", originalMailId);
            return;
        }

        // Cek apakah sudah ada record untuk original mail ini
        MailResponseTime existing = responseTimeRepository
                .findByOriginalMailId(originalMailId)
                .orElse(null);
        if (existing != null) {
            log.debug("Response time already recorded for original mail {}", originalMailId);
            return;
        }

        // Hitung response time dalam detik
        LocalDateTime originalDate = originalMail.getCreatedDate();
        LocalDateTime replyDate = replyMail.getCreatedDate();
        if (originalDate == null || replyDate == null) {
            log.warn("Missing date for original {} or reply {}", originalMailId, replyMail.getId());
            return;
        }
        long responseSeconds = Duration.between(originalDate, replyDate).getSeconds();
        if (responseSeconds < 0) {
            log.warn("Response time negative for mail {}, ignoring", event.mailId());
            return;
        }

        MailResponseTime record = new MailResponseTime();
        record.setOriginalMail(originalMail);
        record.setOriginalDate(originalDate);
        record.setReplyMail(replyMail);
        record.setReplyDate(replyDate);
        record.setMailType(replyMail.getMailType());
        record.setMailCategory(replyMail.getMailCategory());
        record.setResponseTime((int) responseSeconds);
        // createdAt and updatedAt diisi otomatis oleh @PrePersist

        responseTimeRepository.save(record);
        log.info("Recorded response time {} seconds for original mail {}", responseSeconds, originalMailId);
    }
}
