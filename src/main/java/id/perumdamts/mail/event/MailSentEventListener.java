package id.perumdamts.mail.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class MailSentEventListener {

    private static final Logger log = LoggerFactory.getLogger(MailSentEventListener.class);

    private final CacheManager cacheManager;

    public MailSentEventListener(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @TransactionalEventListener
    @Async
    public void onMailSent(MailSentEvent event) {
        log.info("Mail sent: mailId={}, sender={}, recipients={}",
                event.mailId(), event.senderName(), event.recipientUserIds().size());

        // Evict mailStats cache for all recipients so inbox counters update
        var statsCache = cacheManager.getCache("mailStats");
        if (statsCache != null) {
            event.recipientUserIds().forEach(id -> statsCache.evict("user:" + id));
            // Also evict sender's stats
            statsCache.evict("user:" + event.senderId());
        }

        // Evict mailFolder cache for recipients so folder counters update
        var folderCache = cacheManager.getCache("mailFolder");
        if (folderCache != null) {
            event.recipientUserIds().forEach(id -> folderCache.evict("user:" + id));
            folderCache.evict("user:" + event.senderId());
        }
    }
}
