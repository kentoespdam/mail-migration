package id.perumdamts.mail.repository.archive.jooq;

import java.util.Optional;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import id.perumdamts.mail.entity.core.MailArchiveNotif;

@Repository
public class MailArchiveNotifQueryRepository {
    private final DSLContext dsl;

    public MailArchiveNotifQueryRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Optional<MailArchiveNotif> findById(Integer id) {
        Record r = dsl.selectFrom(DSL.table("mail_archive_notif"))
                .where(DSL.field("id").eq(id))
                .fetchOne();
        if (r == null) return Optional.empty();
        MailArchiveNotif e = new MailArchiveNotif();
        e.setId(r.get("id", Integer.class));
        e.setMailArchiveId(r.get("mail_archive_id", Long.class));
        e.setNotifFlag(r.get("notif_flag", Integer.class));
        e.setInsertDate(r.get("insert_date", java.time.LocalDateTime.class));
        e.setProcessedDate(r.get("processed_date", java.time.LocalDateTime.class));
        e.setUpdatedAt(r.get("updated_at", java.time.LocalDateTime.class));
        return Optional.of(e);
    }
}
