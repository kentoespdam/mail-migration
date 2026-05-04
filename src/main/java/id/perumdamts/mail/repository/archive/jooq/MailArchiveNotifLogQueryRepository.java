package id.perumdamts.mail.repository.archive.jooq;

import id.perumdamts.mail.entity.core.MailArchiveNotifLog;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MailArchiveNotifLogQueryRepository {
    private final DSLContext dsl;

    public MailArchiveNotifLogQueryRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Optional<MailArchiveNotifLog> findById(Long id) {
        Record r = dsl.selectFrom(DSL.table("mail_archive_notif_log"))
                .where(DSL.field("id").eq(id))
                .fetchOne();
        if (r == null) return Optional.empty();
        // Map record to entity (simple mapper)
        MailArchiveNotifLog e = new MailArchiveNotifLog();
        e.setId(r.get("id", Long.class));
        e.setMailArchiveId(r.get("mail_archive_id", Long.class));
        e.setUserId(r.get("user_id", Long.class));
        e.setNotifDate(r.get("notif_date", java.time.LocalDateTime.class));
        return Optional.of(e);
    }
}