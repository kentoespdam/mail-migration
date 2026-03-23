package id.perumdamts.mail.service.mail.listener;

import id.perumdamts.mail.config.CacheConfig;
import id.perumdamts.mail.domain.event.MailSentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class MailSentEventListener {

    private static final Logger log = LoggerFactory.getLogger(MailSentEventListener.class);

    @Async
    @EventListener
    @CacheEvict(value = CacheConfig.CacheNames.MAIL_STATS, allEntries = true)
    public void handleMailSent(MailSentEvent event) {
        log.info("Mail sent: mailId={}, sender={}, recipients={}",
                event.mailId(), event.senderName(), event.recipientUserIds().size());
    }
}
