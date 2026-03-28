package id.perumdamts.mail.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PublicationNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(PublicationNotificationListener.class);

    @TransactionalEventListener
    @Async
    public void onPublished(PublicationPublishedEvent event) {
        log.info("Publication published: id={}, by={}",
                event.publicationId(), event.publisherName());

        // TODO: implement notification (push, email, etc.)
    }
}
