package id.perumdamts.mail.repository.core.jooq;

import id.perumdamts.mail.dto.core.archive.MailArchiveNotifResponse;
import id.perumdamts.mail.entity.core.MailArchive;
import id.perumdamts.mail.util.SqidsEncoder;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@Repository
public class MailArchiveNotifQueryRepository {
    private final DSLContext dsl;
    private final SqidsEncoder encoder;

    public MailArchiveNotifQueryRepository(DSLContext dsl, SqidsEncoder encoder) {
        this.dsl = dsl;
        this.encoder = encoder;
    }

    public List<MailArchiveNotifResponse> findPending() {
        return dsl.select(
                field("id"),
                field("mail_archive_id"),
                field("notif_flag"),
                field("insert_date"),
                field("processed_date"),
                field("updated_at")
            )
            .from(table("mail_archive_notif"))
            .where(field("processed_date").isNull())
            .fetch(r -> new MailArchiveNotifResponse(
                r.get("id", Integer.class),
                encoder.encode(MailArchive.class, r.get("mail_archive_id", Long.class)),
                r.get("notif_flag", Integer.class),
                r.get("insert_date", LocalDateTime.class),
                r.get("processed_date", LocalDateTime.class),
                r.get("updated_at", LocalDateTime.class)
            ));
    }

    public Optional<MailArchiveNotifResponse> findById(Integer id) {
        return dsl.select(
                field("id"),
                field("mail_archive_id"),
                field("notif_flag"),
                field("insert_date"),
                field("processed_date"),
                field("updated_at")
            )
            .from(table("mail_archive_notif"))
            .where(field("id").eq(id))
            .fetchOptional(r -> new MailArchiveNotifResponse(
                r.get("id", Integer.class),
                encoder.encode(MailArchive.class, r.get("mail_archive_id", Long.class)),
                r.get("notif_flag", Integer.class),
                r.get("insert_date", LocalDateTime.class),
                r.get("processed_date", LocalDateTime.class),
                r.get("updated_at", LocalDateTime.class)
            ));
    }
}
