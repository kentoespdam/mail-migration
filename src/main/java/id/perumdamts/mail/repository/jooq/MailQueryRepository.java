package id.perumdamts.mail.repository.jooq;

import id.perumdamts.mail.api.dto.mail.MailReportRequest;
import id.perumdamts.mail.api.dto.mail.MailReportResponse;
import id.perumdamts.mail.api.dto.mail.MailSearchRequest;
import id.perumdamts.mail.api.dto.mail.MailSummaryResponse;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.jooq.impl.DSL.*;

@Repository
public class MailQueryRepository {

    private final DSLContext dsl;

    public MailQueryRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<MailSummaryResponse> findMailsInFolder(Integer userId, Integer folderId,
                                                       int offset, int limit,
                                                       boolean sortAsc) {
        var baseQuery = dsl.select(
                        field("m.m_id").as("id"),
                        field("m.m_no").as("mailNumber"),
                        field("m.m_date").as("mailDate"),
                        field("m.m_subject").as("subject"),
                        field("m.m_created_by_name").as("createdByName"),
                        field("m.m_to_str").as("toStr"),
                        field("ut.read_status").as("readStatus"),
                        field("ut.folder_id").as("folderId"),
                        field("m.m_attachment_qty").as("attachmentQty"),
                        field("m.m_created_date").as("createdDate"),
                        field("mt.mail_type").as("mailTypeName"),
                        field("mc.mcat_name").as("mailCategoryName"),
                        field("ut.restore_folder_id").as("restoreFolderId")
                )
                .from(table("sys_user_task").as("ut"))
                .join(table("mail").as("m")).on(field("m.m_id").eq(field("ut.tm_id")))
                .leftJoin(table("mail_type").as("mt")).on(field("mt.mail_type_id").eq(field("m.m_type")))
                .leftJoin(table("mail_category").as("mc")).on(field("mc.mcat_id").eq(field("m.m_category")))
                .where(field("ut.user_id").eq(userId))
                .and(field("ut.folder_id").eq(folderId));

        var sortField = sortAsc ? field("m.m_created_date").asc() : field("m.m_created_date").desc();

        return baseQuery
                .orderBy(sortField)
                .limit(limit)
                .offset(offset)
                .fetchInto(MailSummaryResponse.class);
    }

    public List<MailSummaryResponse> searchMails(MailSearchRequest request) {
        Condition condition = field("m.m_status").eq(1)
                .and(field("m.m_id").eq(field("m.m_root_id")));

        if (request.keyword() != null && !request.keyword().isBlank()) {
            String kw = "%" + request.keyword() + "%";
            condition = condition.and(
                    field("m.m_subject").likeIgnoreCase(kw)
                            .or(field("m.m_no").likeIgnoreCase(kw))
                            .or(field("m.m_content").likeIgnoreCase(kw))
                            .or(field("m.m_created_by_name").likeIgnoreCase(kw))
                            .or(field("m.m_to_str").likeIgnoreCase(kw))
            );
        }

        if (request.mailTypeId() != null) {
            condition = condition.and(field("m.m_type").eq(request.mailTypeId()));
        }
        if (request.mailCategoryId() != null) {
            condition = condition.and(field("m.m_category").eq(request.mailCategoryId()));
        }
        if (request.startDate() != null && request.endDate() != null) {
            condition = condition.and(field("m.m_date").between(request.startDate(), request.endDate()));
        }

        return dsl.select(
                        field("m.m_id").as("id"),
                        field("m.m_no").as("mailNumber"),
                        field("m.m_date").as("mailDate"),
                        field("m.m_subject").as("subject"),
                        field("m.m_created_by_name").as("createdByName"),
                        field("m.m_to_str").as("toStr"),
                        inline(1).as("readStatus"),
                        inline(0).as("folderId"),
                        field("m.m_attachment_qty").as("attachmentQty"),
                        field("m.m_created_date").as("createdDate"),
                        field("mt.mail_type").as("mailTypeName"),
                        field("mc.mcat_name").as("mailCategoryName")
                )
                .from(table("mail").as("m"))
                .leftJoin(table("mail_type").as("mt")).on(field("mt.mail_type_id").eq(field("m.m_type")))
                .leftJoin(table("mail_category").as("mc")).on(field("mc.mcat_id").eq(field("m.m_category")))
                .where(condition)
                .orderBy(field("m.m_created_date").desc())
                .limit(request.size())
                .offset(request.offset())
                .fetchInto(MailSummaryResponse.class);
    }

    public List<MailSummaryResponse> findThread(Integer mailId) {
        // Resolve root
        Integer rootId = dsl.select(field("m_root_id"))
                .from(table("mail"))
                .where(field("m_id").eq(mailId))
                .fetchOneInto(Integer.class);

        if (rootId == null) rootId = mailId;

        return dsl.select(
                        field("m.m_id").as("id"),
                        field("m.m_no").as("mailNumber"),
                        field("m.m_date").as("mailDate"),
                        field("m.m_subject").as("subject"),
                        field("m.m_created_by_name").as("createdByName"),
                        field("m.m_to_str").as("toStr"),
                        inline(1).as("readStatus"),
                        inline(0).as("folderId"),
                        field("m.m_attachment_qty").as("attachmentQty"),
                        field("m.m_created_date").as("createdDate"),
                        field("mt.mail_type").as("mailTypeName"),
                        field("mc.mcat_name").as("mailCategoryName")
                )
                .from(table("mail").as("m"))
                .leftJoin(table("mail_type").as("mt")).on(field("mt.mail_type_id").eq(field("m.m_type")))
                .leftJoin(table("mail_category").as("mc")).on(field("mc.mcat_id").eq(field("m.m_category")))
                .where(field("m.m_root_id").eq(rootId))
                .and(field("m.m_status").gt(0))
                .orderBy(field("m.m_created_date").asc())
                .fetchInto(MailSummaryResponse.class);
    }

    public List<MailReportResponse> getReport(MailReportRequest request) {
        Condition condition = field("m.m_status").eq(1);

        if (request.mailTypeId() != null) {
            condition = condition.and(field("m.m_type").eq(request.mailTypeId()));
        }
        if (request.mailCategoryId() != null) {
            condition = condition.and(field("m.m_category").eq(request.mailCategoryId()));
        }
        if (request.startDate() != null && request.endDate() != null) {
            condition = condition.and(field("m.m_date").between(request.startDate(), request.endDate()));
        }

        return dsl.select(
                        field("mt.mail_type").as("mailTypeName"),
                        field("mc.mcat_name").as("mailCategoryName"),
                        count().as("totalMails"),
                        count(
                                case_()
                                        .when(field("ut.read_status").eq(1), inline(1))
                        ).as("totalRead"),
                        count(
                                case_()
                                        .when(field("ut.read_status").eq(0), inline(1))
                        ).as("totalUnread")
                )
                .from(table("mail").as("m"))
                .join(table("sys_user_task").as("ut")).on(field("ut.tm_id").eq(field("m.m_id")))
                .leftJoin(table("mail_type").as("mt")).on(field("mt.mail_type_id").eq(field("m.m_type")))
                .leftJoin(table("mail_category").as("mc")).on(field("mc.mcat_id").eq(field("m.m_category")))
                .where(condition)
                .groupBy(field("mt.mail_type"), field("mc.mcat_name"))
                .orderBy(field("mt.mail_type"), field("mc.mcat_name"))
                .fetchInto(MailReportResponse.class);
    }
}
