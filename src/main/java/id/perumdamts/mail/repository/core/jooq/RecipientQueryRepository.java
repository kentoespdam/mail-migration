package id.perumdamts.mail.repository.core.jooq;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.jooq.impl.DSL.*;

@Repository
public class RecipientQueryRepository {

    private final DSLContext dsl;

    public RecipientQueryRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * Find distinct recipients across all mails in a thread (by root_mail_id).
     * Returns unique (user_id, emp_id, emp_name, pos_name) tuples,
     * picking the most recent record per user.
     */
    public List<ThreadRecipientRow> findDistinctThreadRecipients(Integer rootMailId) {
        return dsl.select(
                        field("r.user_id"),
                        field("r.emp_id"),
                        field("r.emp_name"),
                        field("r.pos_name"),
                        field("r.pos_id")
                )
                .from(table("mail_recipient").as("r"))
                .join(table("mail").as("m")).on(field("m.m_id").eq(field("r.mail_id")))
                .where(field("m.m_root_id").eq(rootMailId))
                .and(field("m.m_status").gt(0))
                .groupBy(field("r.user_id"))
                .orderBy(max(field("r.id")).desc())
                .fetchInto(ThreadRecipientRow.class);
    }

    public record ThreadRecipientRow(
            Integer userId,
            Integer empId,
            String empName,
            String posName,
            Integer posId
    ) {}
}
