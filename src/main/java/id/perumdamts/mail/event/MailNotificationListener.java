package id.perumdamts.mail.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listener untuk handle mail notification secara async.
 * Email notification dikirim setelah transaksi commit.
 */
@Service
public class MailNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(MailNotificationListener.class);

    /**
     * Handle MailSentEvent untuk mengirim email notification.
     * Dijalankan secara async setelah transaksi commit.
     */
    @TransactionalEventListener
    @Async
    public void onMailSent(MailSentEvent event) {
        log.info("Sending mail notification: mailId={}, sender={}, recipients={}",
                event.mailId(), event.senderName(), event.recipientUserIds().size());

        // TODO: Implement email notification logic
        // - Get mail details
        // - Get recipient emails from HR service
        // - Send email via SMTP / mail service
        // - Log notification result

        for (Integer recipientId : event.recipientUserIds()) {
            sendNotificationToRecipient(event.mailId(), recipientId);
        }
    }

    /**
     * Send notification ke satu recipient.
     * Placeholder untuk implementasi email notification.
     */
    private void sendNotificationToRecipient(Integer mailId, Integer recipientId) {
        // TODO: Implement actual email sending
        log.debug("Notification sent to recipient {}: mail {}", recipientId, mailId);
    }
}
