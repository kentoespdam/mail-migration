package id.perumdamts.mail.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listener untuk update statistik mail secara async.
 * Update kategori dan organisasi statistik setelah mail dikirim.
 */
@Service
public class MailStatisticListener {

    private static final Logger log = LoggerFactory.getLogger(MailStatisticListener.class);

    /**
     * Handle MailSentEvent untuk update statistik.
     * Dijalankan secara async setelah transaksi commit.
     */
    @TransactionalEventListener
    @Async
    public void onMailSent(MailSentEvent event) {
        log.info("Updating mail statistics: mailId={}, sender={}",
                event.mailId(), event.senderId());

        // TODO: Implement statistic update logic
        // - Update mail category statistics (count per category)
        // - Update organization statistics (count per org unit)
        // - Update daily/weekly/monthly aggregates
        // - Cache invalidation untuk statistic cache
    }
}
