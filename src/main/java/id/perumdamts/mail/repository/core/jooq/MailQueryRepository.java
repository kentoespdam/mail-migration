package id.perumdamts.mail.repository.core.jooq;

import id.perumdamts.mail.dto.core.folder.MailFolderLookup;
import id.perumdamts.mail.dto.core.mail.*;
import id.perumdamts.mail.dto.master.mailCategory.MailCategoryLookup;
import id.perumdamts.mail.dto.master.mailType.MailTypeLookup;
import id.perumdamts.mail.entity.core.Mail;
import id.perumdamts.mail.entity.core.MailRecipient;
import id.perumdamts.mail.entity.master.MailCategory;
import id.perumdamts.mail.entity.master.MailType;
import id.perumdamts.mail.util.SqidsEncoder;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SortField;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.jooq.impl.DSL.*;

@Repository
public class MailQueryRepository {

        private final DSLContext dsl;
        private final SqidsEncoder encoder;

        public MailQueryRepository(DSLContext dsl, SqidsEncoder encoder) {
                this.dsl = dsl;
                this.encoder = encoder;
        }

        public List<MailSummaryResponse> findMailsInFolder(Long userId, Long folderId,
                        int offset, int limit,
                        SortField<?> sort, String keyword,
                        LocalDate sdate, LocalDate edate) {
                Condition condition = field("ut.user_id").eq(userId)
                                .and(field("ut.folder_id").eq(folderId));

                if (sdate != null && edate != null) {
                        condition = condition.and(
                                        field("m.m_date").between(sdate, edate)
                                                        .or(field("ut.read_status").eq(0)));
                }

                if (keyword != null && !keyword.isBlank()) {
                        String kw = "%" + keyword + "%";
                        condition = condition.and(
                                        condition("MATCH(m.m_subject, m.m_content) AGAINST (? IN BOOLEAN MODE)", keyword)
                                                        .or(field("m.m_no").likeIgnoreCase(kw))
                                                        .or(field("m.m_created_by_name").likeIgnoreCase(kw)));
                }

                return dsl.select(
                                field("m.m_id").as("id"),
                                field("m.m_no").as("mailNumber"),
                                field("m.m_date").as("mailDate"),
                                field("m.m_subject").as("subject"),
                                field("m.m_created_by_name").as("createdByName"),
                                field("m.m_created_by").as("createdBy"),
                                field("m.m_to_str").as("toStr"),
                                field("ut.read_status").as("readStatus"),
                                field("ut.folder_id").as("folderId"),
                                field("m.m_attachment_qty").as("attachmentQty"),
                                field("m.m_created_date").as("createdDate"),
                                field("mt.mail_type").as("mailTypeName"),
                                field("mc.mcat_name").as("mailCategoryName"),
                                inline("N/A").as("circulationName"),
                                field("ut.restore_folder_id").as("restoreFolderId"),
                                inline("").as("restoreFolderName"),
                                field("m.m_root_id").as("rootMailId"),
                                field("m.m_parent_id").as("parentMailId"),
                                count().over().as("totalCount"))
                                .from(table("sys_user_task").as("ut"))
                                .join(table("mail").as("m")).on(field("m.m_id").eq(field("ut.tm_id")))
                                .leftJoin(table("mail_type").as("mt"))
                                .on(field("mt.mail_type_id").eq(field("m.m_type")))
                                .leftJoin(table("mail_category").as("mc"))
                                .on(field("mc.mcat_id").eq(field("m.m_category")))
                                .where(condition)
                                .orderBy(sort)
                                .limit(limit)
                                .offset(offset)
                                .fetch(this::mapToSummaryResponse);
        }

        public List<MailSummaryResponse> searchMails(MailSearchRequest request) {
                Condition condition = field("m.m_status").eq(1)
                                .and(field("m.m_id").eq(field("m.m_root_id")));

                if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
                        String kw = "%" + request.getKeyword() + "%";
                        condition = condition.and(
                                        condition("MATCH(m.m_subject, m.m_content) AGAINST (? IN BOOLEAN MODE)", request.getKeyword())
                                                        .or(field("m.m_no").likeIgnoreCase(kw))
                                                        .or(field("m.m_created_by_name").likeIgnoreCase(kw))
                                                        .or(field("m.m_to_str").likeIgnoreCase(kw)));
                }

                if (request.getMailTypeId() != null && !request.getMailTypeId().isBlank()) {
                        condition = condition.and(
                                        field("m.m_type").eq(encoder.decode(MailType.class, request.getMailTypeId())));
                }
                if (request.getMailCategoryId() != null && !request.getMailCategoryId().isBlank()) {
                        condition = condition
                                        .and(field("m.m_category").eq(encoder.decode(MailCategory.class,
                                                        request.getMailCategoryId())));
                }
                if (request.getStartDate() != null && request.getEndDate() != null) {
                        condition = condition
                                        .and(field("m.m_date").between(request.getStartDate(), request.getEndDate()));
                }
                if (request.getHasAttachment() != null && request.getHasAttachment()) {
                        condition = condition.and(field("m.m_attachment_qty").gt(0));
                }
                if (request.getSenderId() != null && !request.getSenderId().isBlank()) {
                        condition = condition
                                        .and(field("m.m_created_by").eq(
                                                        encoder.decode(MailRecipient.class, request.getSenderId())));
                }

                return dsl.select(
                                field("m.m_id").as("id"),
                                field("m.m_no").as("mailNumber"),
                                field("m.m_date").as("mailDate"),
                                field("m.m_subject").as("subject"),
                                field("m.m_created_by_name").as("createdByName"),
                                field("m.m_created_by").as("createdBy"),
                                field("m.m_to_str").as("toStr"),
                                inline(1).as("readStatus"),
                                inline(0L).as("folderId"),
                                field("m.m_attachment_qty").as("attachmentQty"),
                                field("m.m_created_date").as("createdDate"),
                                field("mt.mail_type").as("mailTypeName"),
                                field("mc.mcat_name").as("mailCategoryName"),
                                field("m.m_root_id").as("rootMailId"),
                                field("m.m_parent_id").as("parentMailId"),
                                count().over().as("totalCount"))
                                .from(table("mail").as("m"))
                                .leftJoin(table("mail_type").as("mt"))
                                .on(field("mt.mail_type_id").eq(field("m.m_type")))
                                .leftJoin(table("mail_category").as("mc"))
                                .on(field("mc.mcat_id").eq(field("m.m_category")))
                                .where(condition)
                                .orderBy(request.toSortField())
                                .limit(request.getSize())
                                .offset(request.offset())
                                .fetch(this::mapToSummaryResponse);
        }

        public List<MailSummaryResponse> findThread(Long mailId) {
                Long rootId = dsl.select(field("m_root_id", Long.class))
                                .from(table("mail"))
                                .where(field("m_id").eq(mailId))
                                .fetchOneInto(Long.class);

                if (rootId == null)
                        rootId = mailId;

                return dsl.select(
                                field("m.m_id").as("id"),
                                field("m.m_no").as("mailNumber"),
                                field("m.m_date").as("mailDate"),
                                field("m.m_subject").as("subject"),
                                field("m.m_created_by_name").as("createdByName"),
                                field("m.m_created_by").as("createdBy"),
                                field("m.m_to_str").as("toStr"),
                                inline(1).as("readStatus"),
                                inline(0L).as("folderId"),
                                field("m.m_attachment_qty").as("attachmentQty"),
                                field("m.m_created_date").as("createdDate"),
                                field("mt.mail_type").as("mailTypeName"),
                                field("mc.mcat_name").as("mailCategoryName"),
                                inline("N/A").as("circulationName"),
                                field("ut.restore_folder_id").as("restoreFolderId"),
                                inline("").as("restoreFolderName"),
                                field("m.m_root_id").as("rootMailId"),
                                field("m.m_parent_id").as("parentMailId"),
                                count().over().as("totalCount"))
                                .from(table("mail").as("m"))
                                .leftJoin(table("mail_type").as("mt"))
                                .on(field("mt.mail_type_id").eq(field("m.m_type")))
                                .leftJoin(table("mail_category").as("mc"))
                                .on(field("mc.mcat_id").eq(field("m.m_category")))
                                .leftJoin(table("sys_user_task").as("ut")).on(field("ut.tm_id").eq(field("m.m_id")))
                                .where(field("m.m_root_id").eq(rootId))
                                .and(field("m.m_status").gt(0))
                                .orderBy(field("m.m_created_date").asc())
                                .fetch(this::mapToSummaryResponse);
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

        public List<MailReportResponse> getReport(MailReportRequest request) {
                Condition condition = field("m.m_status").eq(1);

                if (request.getMailTypeId() != null && !request.getMailTypeId().isBlank()) {
                        condition = condition.and(
                                        field("m.m_type").eq(encoder.decode(MailType.class, request.getMailTypeId())));
                }
                if (request.getMailCategoryId() != null && !request.getMailCategoryId().isBlank()) {
                        condition = condition
                                        .and(field("m.m_category").eq(encoder.decode(MailCategory.class,
                                                        request.getMailCategoryId())));
                }
                if (request.getStartDate() != null && request.getEndDate() != null) {
                        condition = condition
                                        .and(field("m.m_date").between(request.getStartDate(), request.getEndDate()));
                }

                return dsl.select(
                                field("mt.mail_type").as("mailTypeName"),
                                field("mc.mcat_name").as("mailCategoryName"),
                                count().as("totalMails"),
                                count(
                                                case_()
                                                                .when(field("ut.read_status").eq(1), inline(1)))
                                                .as("totalRead"),
                                count(
                                                case_()
                                                                .when(field("ut.read_status").eq(0), inline(1)))
                                                .as("totalUnread"),
                                count().over().as("totalCount"))
                                .from(table("mail").as("m"))
                                .join(table("sys_user_task").as("ut")).on(field("ut.tm_id").eq(field("m.m_id")))
                                .leftJoin(table("mail_type").as("mt"))
                                .on(field("mt.mail_type_id").eq(field("m.m_type")))
                                .leftJoin(table("mail_category").as("mc"))
                                .on(field("mc.mcat_id").eq(field("m.m_category")))
                                .where(condition)
                                .groupBy(field("mt.mail_type"), field("mc.mcat_name"))
                                .orderBy(request.toSortField())
                                .limit(request.getSize())
                                .offset(request.offset())
                                .fetch(r -> new MailReportResponse(
                                                r.get("mailTypeName", String.class),
                                                r.get("mailCategoryName", String.class),
                                                r.get("totalMails", Long.class),
                                                r.get("totalRead", Long.class),
                                                r.get("totalUnread", Long.class),
                                                r.get("totalCount", Long.class)));
        }

        private MailSummaryResponse mapToSummaryResponse(org.jooq.Record r) {
                return new MailSummaryResponse(
                                encoder.encode(Mail.class, r.get("id", Long.class)),
                                r.get("mailNumber", String.class),
                                r.get("mailDate", LocalDate.class),
                                r.get("subject", String.class),
                                new MailComponentDto.MailAuditInfoDto(
                                                r.get("createdBy") != null
                                                                ? encoder.encode(Mail.class,
                                                                                r.get("createdBy", Long.class))
                                                                : null,
                                                r.get("createdByName", String.class),
                                                r.get("createdDate", LocalDateTime.class),
                                                null),
                                new MailComponentDto.MailSummaryInfoDto(
                                                r.get("attachmentQty", Integer.class),
                                                r.get("toStr", String.class)),
                                r.get("readStatus", Integer.class),
                                r.get("folderId") != null ? String.valueOf(r.get("folderId")) : null,
                                new MailTypeLookup(null, r.get("mailTypeName", String.class)),
                                new MailCategoryLookup(null, r.get("mailCategoryName", String.class)),
                                r.get("circulationName") != null ? r.get("circulationName", String.class) : null,
                                new MailFolderLookup(
                                                r.get("restoreFolderId") != null
                                                                ? String.valueOf(r.get("restoreFolderId"))
                                                                : null,
                                                r.get("restoreFolderName") != null
                                                                ? r.get("restoreFolderName", String.class)
                                                                : null),
                                new MailComponentDto.MailThreadInfoDto(
                                                r.get("rootMailId") != null
                                                                ? encoder.encode(Mail.class,
                                                                                r.get("rootMailId", Long.class))
                                                                : null,
                                                r.get("parentMailId") != null
                                                                ? encoder.encode(Mail.class,
                                                                                r.get("parentMailId", Long.class))
                                                                : null),
                                r.get("totalCount") != null ? r.get("totalCount", Long.class) : null);
        }
}
