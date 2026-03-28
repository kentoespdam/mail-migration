package id.perumdamts.mail.event;

import id.perumdamts.mail.entity.core.Publication;
import id.perumdamts.mail.repository.core.jpa.PublicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class PublicationNotifScheduler {

    private static final Logger log = LoggerFactory.getLogger(PublicationNotifScheduler.class);

    private final PublicationRepository publicationRepository;

    public PublicationNotifScheduler(PublicationRepository publicationRepository) {
        this.publicationRepository = publicationRepository;
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void retryUnnotified() {
        List<Publication> unnotified = publicationRepository.findByNotifFlag(1);
        if (unnotified.isEmpty()) return;

        log.info("Retrying notification for {} publications", unnotified.size());
        for (Publication pub : unnotified) {
            try {
                // TODO: send notification
                pub.setNotifFlag(0);
                publicationRepository.save(pub);
            } catch (Exception e) {
                log.warn("Failed to notify publication id={}: {}", pub.getId(), e.getMessage());
            }
        }
    }
}
