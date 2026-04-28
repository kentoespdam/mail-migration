package id.perumdamts.mail.event;

import id.perumdamts.mail.config.CacheConfig;
import id.perumdamts.mail.repository.core.jooq.MailQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailQueryCacheListener {

    private final CacheManager cacheManager;
    private final MailQueryRepository mailQueryRepository;

    @EventListener
    public void handleMailSent(MailSentEvent event) {
        Long rootId = mailQueryRepository.resolveRootId(event.mailId());
        log.debug("Evicting mail thread cache for rootId: {}", rootId);
        Objects.requireNonNull(cacheManager.getCache(CacheConfig.CacheNames.MAIL_THREAD))
                .evict(rootId);
    }

    @EventListener
    public void handleRecipientRead(RecipientReadEvent event) {
        log.debug("Evicting mail tracking and read status cache for mailId: {}", event.mailId());
        Objects.requireNonNull(cacheManager.getCache(CacheConfig.CacheNames.MAIL_TRACKING))
                .evict(event.mailId());
        Objects.requireNonNull(cacheManager.getCache(CacheConfig.CacheNames.MAIL_READ_STATUS))
                .evict(event.mailId());
    }
}
