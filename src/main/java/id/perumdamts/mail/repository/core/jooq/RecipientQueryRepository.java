package id.perumdamts.mail.repository.core.jooq;

import id.perumdamts.mail.dto.core.mail.MailTrackingResponse;
import id.perumdamts.mail.dto.core.mail.RecipientReadStatusResponse;
import id.perumdamts.mail.dto.core.recipient.RecipientComponentDto;
import id.perumdamts.mail.dto.core.recipient.RecipientResponse;
import id.perumdamts.mail.entity.core.MailRecipient;
import id.perumdamts.mail.enums.CirculationType;
import id.perumdamts.mail.util.SqidsEncoder;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static org.jooq.impl.DSL.*;

@Repository
public class RecipientQueryRepository {

    private final DSLContext dsl;
    private final SqidsEncoder encoder;

    public RecipientQueryRepository(DSLContext dsl, SqidsEncoder encoder) {
        this.dsl = dsl;
        this.encoder = encoder;
    }

    public List<RecipientResponse> findByMailId(Long mailId) {
        return dsl.select()
                .from(table("mail_recipient").as("r"))
                .where(field("r.mail_id").eq(mailId))
                .orderBy(field("r.id").asc())
                .fetch(this::mapToRecipientResponse);
    }

    private RecipientResponse mapToRecipientResponse(org.jooq.Record r) {
        Integer circulation = r.get(field("r.circulation"), Integer.class);
        return new RecipientResponse(
                encoder.encode(MailRecipient.class, r.get(field("r.id"), Long.class)),
                new RecipientComponentDto.EmployeeInfoDto(
                        r.get(field("r.user_id"), Long.class) != null
                                ? encoder.encode(MailRecipient.class, r.get(field("r.user_id"), Long.class))
                                : null,
                        r.get(field("r.emp_id"), Long.class) != null
                                ? encoder.encode(MailRecipient.class, r.get(field("r.emp_id"), Long.class))
                                : null,
                        r.get(field("r.emp_name"), String.class),
                        r.get(field("r.pos_name"), String.class)),
                new RecipientComponentDto.CirculationInfoDto(
                        String.valueOf(circulation),
                        circulation != null ? CirculationType.fromDbValue(circulation).name() : "UNKNOWN"),
                new RecipientComponentDto.NotificationInfoDto(
                        r.get(field("r.email"), Integer.class),
                        r.get(field("r.sms"), Integer.class),
                        r.get(field("r.is_notified"), Boolean.class),
                        r.get(field("r.is_read"), Boolean.class),
                        r.get(field("r.folder_position"), Integer.class)));
    }

    public List<MailTrackingResponse> findTracking(Long mailId) {
        return dsl.select(
                field("r.id").as("recipientId"),
                field("r.emp_name").as("empName"),
                field("r.pos_name").as("posName"),
                case_()
                        .when(field("r.circulation").eq(1), inline("DISPOSISI"))
                        .when(field("r.circulation").eq(2), inline("MEMO_MANDIRI"))
                        .when(field("r.circulation").eq(3), inline("MEMO"))
                        .when(field("r.circulation").eq(4), inline("CC"))
                        .when(field("r.circulation").eq(5), inline("REPLY"))
                        .when(field("r.circulation").eq(6), inline("FORWARD"))
                        .otherwise(inline("UNKNOWN"))
                        .as("circulationName"),
                field("r.is_read").as("isRead"),
                field("ut.read_date").as("readDate"))
                .from(table("mail_recipient").as("r"))
                .leftJoin(table("sys_user_task").as("ut"))
                .on(field("ut.user_id").eq(field("r.user_id"))
                        .and(field("ut.tm_id").eq(field("r.mail_id"))))
                .where(field("r.mail_id").eq(mailId))
                .orderBy(field("r.id").asc())
                .fetch(r -> new MailTrackingResponse(
                        encoder.encode(MailRecipient.class, r.get("recipientId", Long.class)),
                        r.get("empName", String.class),
                        r.get("posName", String.class),
                        r.get("circulationName", String.class),
                        r.get("isRead", Boolean.class),
                        r.get("readDate", LocalDateTime.class)));
    }

    public List<RecipientReadStatusResponse> findReadStatus(Long mailId) {
        return dsl.select(
                field("r.id").as("recipientId"),
                field("r.user_id").as("userId"),
                field("r.emp_name").as("empName"),
                field("r.pos_name").as("posName"),
                case_()
                        .when(field("r.circulation").eq(1), inline("DISPOSISI"))
                        .when(field("r.circulation").eq(2), inline("MEMO_MANDIRI"))
                        .when(field("r.circulation").eq(3), inline("MEMO"))
                        .when(field("r.circulation").eq(4), inline("CC"))
                        .when(field("r.circulation").eq(5), inline("REPLY"))
                        .when(field("r.circulation").eq(6), inline("FORWARD"))
                        .otherwise(inline("UNKNOWN"))
                        .as("circulationName"),
                field("ut.read_status").as("readStatus"),
                field("ut.read_date").as("readDate"))
                .from(table("mail_recipient").as("r"))
                .leftJoin(table("sys_user_task").as("ut"))
                .on(field("ut.user_id").eq(field("r.user_id"))
                        .and(field("ut.tm_id").eq(field("r.mail_id"))))
                .where(field("r.mail_id").eq(mailId))
                .orderBy(field("r.id").asc())
                .fetch(r -> new RecipientReadStatusResponse(
                        encoder.encode(MailRecipient.class, r.get("recipientId", Long.class)),
                        encoder.encode(MailRecipient.class, r.get("userId", Long.class)), // Placeholder
                        r.get("empName", String.class),
                        r.get("posName", String.class),
                        r.get("circulationName", String.class),
                        r.get("readStatus", Integer.class),
                        r.get("readDate", LocalDateTime.class)));
    }

    /**
     * Find distinct recipients across all mails in a thread (by root_mail_id).
     * Returns unique (user_id, emp_id, emp_name, pos_name) tuples,
     * picking the most recent record per user.
     */
    public List<ThreadRecipientRow> findDistinctThreadRecipients(Long rootMailId) {
        return dsl.select(
                field("r.user_id"),
                field("r.emp_id"),
                field("r.emp_name"),
                field("r.pos_name"),
                field("r.pos_id"))
                .from(table("mail_recipient").as("r"))
                .join(table("mail").as("m")).on(field("m.m_id").eq(field("r.mail_id")))
                .where(field("m.m_root_id").eq(rootMailId))
                .and(field("m.m_status").gt(0))
                .groupBy(field("r.user_id"))
                .orderBy(max(field("r.id")).desc())
                .fetchInto(ThreadRecipientRow.class);
    }

    public record ThreadRecipientRow(
            Long userId,
            Long empId,
            String empName,
            String posName,
            Long posId) {
    }
}
