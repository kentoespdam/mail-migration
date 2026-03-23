package id.perumdamts.mail.service.listener;

import id.perumdamts.mail.domain.event.ArchivePublishedEvent;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@Component
public class ArchivePublishedEventListener {

    private static final Logger log = LoggerFactory.getLogger(ArchivePublishedEventListener.class);

    private final DSLContext dsl;

    public ArchivePublishedEventListener(DSLContext dsl) {
        this.dsl = dsl;
    }

    @TransactionalEventListener
    @Async
    public void onArchivePublished(ArchivePublishedEvent event) {
        log.info("Archive published: archiveId={}, publisher={}, positionIds={}",
                event.archiveId(), event.publisherName(), event.accessPositionIds().size());

        // Create notification records for each position that has access
        for (Integer positionId : event.accessPositionIds()) {
            dsl.insertInto(table("mail_archive_notif"))
                    .set(field("mail_archive_id"), event.archiveId())
                    .set(field("user_id"), positionId)
                    .set(field("notif_date"), LocalDateTime.now())
                    .set(field("status"), 0)
                    .execute();
        }
    }
}
