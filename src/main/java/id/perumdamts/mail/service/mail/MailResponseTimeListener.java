package id.perumdamts.mail.service.mail;

import id.perumdamts.mail.domain.event.MailSentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listener untuk track response time mail secara async.
 * Response time tracking untuk SLA monitoring.
 */
@Service
public class MailResponseTimeListener {

    private static final Logger log = LoggerFactory.getLogger(MailResponseTimeListener.class);

    /**
     * Handle MailSentEvent untuk track response time.
     * Dijalankan secara async setelah transaksi commit.
     */
    @TransactionalEventListener
    @Async
    public void onMailSent(MailSentEvent event) {
        log.info("Tracking response time: mailId={}, sender={}",
                event.mailId(), event.senderId());

        // TODO: Implement response time tracking logic
        // - Record mail sent timestamp
        // - Track when recipients read the mail
        // - Calculate response time metrics
        // - Alert if SLA breached (maxResponseDate exceeded)
    }
}
