package id.perumdamts.mail.event;

import id.perumdamts.mail.integration.hr.EmployeeDto;
import id.perumdamts.mail.integration.hr.HrServiceClient;
import id.perumdamts.mail.integration.hr.PageResponse;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@Component
public class ArchivePublishedEventListener {

    private static final Logger log = LoggerFactory.getLogger(ArchivePublishedEventListener.class);

    private final DSLContext dsl;
    private final HrServiceClient hrServiceClient;

    public ArchivePublishedEventListener(DSLContext dsl, HrServiceClient hrServiceClient) {
        this.dsl = dsl;
        this.hrServiceClient = hrServiceClient;
    }

    @TransactionalEventListener
    @Async
    public void onArchivePublished(ArchivePublishedEvent event) {
        log.info("Archive published: archiveId={}, publisher={}, positionIds={}",
                event.archiveId(), event.publisherName(), event.accessPositionIds().size());

        // Resolve positions to user IDs
        Set<Long> userIds = new HashSet<>();
        for (Integer positionId : event.accessPositionIds()) {
            try {
                PageResponse<EmployeeDto> response = hrServiceClient.searchEmployees(
                        null, null, positionId.longValue(), null, null, 0, 100);
                if (response != null && response.content() != null) {
                    response.content().forEach(emp -> userIds.add(emp.id()));
                }
            } catch (Exception e) {
                log.error("Failed to fetch employees for positionId={}: {}", positionId, e.getMessage());
            }
        }

        if (userIds.isEmpty()) {
            log.warn("No users found for archiveId={} positionIds={}", event.archiveId(), event.accessPositionIds());
        } else {
            // Insert into mail_archive_notif_log (fan-out per user)
            var logInsert = dsl.insertInto(table("mail_archive_notif_log"),
                    field("mail_archive_id"), field("user_id"), field("notif_date"));
            for (Long userId : userIds) {
                logInsert.values(event.archiveId(), userId, LocalDateTime.now());
            }
            logInsert.execute();
            log.info("Inserted {} notification logs for archiveId={}", userIds.size(), event.archiveId());
        }

        // Upsert into mail_archive_notif (marker per archive)
        // Check if exists first since we don't have a guaranteed unique constraint on mail_archive_id
        boolean exists = dsl.fetchExists(dsl.selectOne()
                .from(table("mail_archive_notif"))
                .where(field("mail_archive_id").eq(event.archiveId())));

        if (exists) {
            dsl.update(table("mail_archive_notif"))
                    .set(field("notif_flag"), 1)
                    .set(field("processed_date"), LocalDateTime.now())
                    .set(field("updated_at"), LocalDateTime.now())
                    .where(field("mail_archive_id").eq(event.archiveId()))
                    .execute();
        } else {
            dsl.insertInto(table("mail_archive_notif"))
                    .set(field("mail_archive_id"), event.archiveId())
                    .set(field("notif_flag"), 1)
                    .set(field("insert_date"), LocalDateTime.now())
                    .set(field("processed_date"), LocalDateTime.now())
                    .set(field("updated_at"), LocalDateTime.now())
                    .execute();
        }
    }
}
