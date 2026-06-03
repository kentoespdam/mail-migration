package id.perumdamts.mail.repository.core.jooq;

import id.perumdamts.mail.dto.core.folder.MailFolderLookup;
import id.perumdamts.mail.dto.core.mail.*;
import id.perumdamts.mail.dto.id.MailCategoryId;
import id.perumdamts.mail.dto.id.MailFolderId;
import id.perumdamts.mail.dto.id.MailId;
import id.perumdamts.mail.dto.id.MailTypeId;
import id.perumdamts.mail.dto.id.UserId;
import id.perumdamts.mail.dto.master.mailCategory.MailCategoryLookup;
import id.perumdamts.mail.dto.master.mailType.MailTypeLookup;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SortField;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.jooq.impl.DSL.*;

@Repository
public class MailQueryRepository {

        private final DSLContext dsl;

        public MailQueryRepository(DSLContext dsl) {
                this.dsl = dsl;
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

                if (request.getMailTypeId() != null) {
                        condition = condition.and(
                                        field("m.m_type").eq(request.getMailTypeId().value()));
                }
                if (request.getMailCategoryId() != null) {
                        condition = condition
                                        .and(field("m.m_category").eq(request.getMailCategoryId().value()));
                }
                if (request.getStartDate() != null && request.getEndDate() != null) {
                        condition = condition
                                        .and(field("m.m_date").between(request.getStartDate(), request.getEndDate()));
                }
                if (request.getHasAttachment() != null && request.getHasAttachment()) {
                        condition = condition.and(field("m.m_attachment_qty").gt(0));
                }
                if (request.getSenderId() != null) {
                        condition = condition
                                        .and(field("m.m_created_by").eq(request.getSenderId().value()));
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

        public Long resolveRootId(Long mailId) {
                Long rootId = dsl.select(field("m_root_id", Long.class))
                                .from(table("mail"))
                                .where(field("m_id").eq(mailId))
                                .fetchOneInto(Long.class);

                return rootId != null ? rootId : mailId;
        }

        public List<MailSummaryResponse> findThread(Long mailId) {
                Long rootId = resolveRootId(mailId);
                return findThreadByRootId(rootId);
        }

        public List<MailSummaryResponse> findThreadByRootId(Long rootId) {
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

        public List<MailReportResponse> getReport(MailReportRequest request) {
                Condition condition = field("m.m_status").eq(1);

                if (request.getMailTypeId() != null) {
                        condition = condition.and(
                                        field("m.m_type").eq(request.getMailTypeId().value()));
                }
                if (request.getMailCategoryId() != null) {
                        condition = condition
                                        .and(field("m.m_category").eq(request.getMailCategoryId().value()));
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

        public Optional<MailResponse> findById(Long id) {
                return dsl.select(
                                field("m.m_id").as("id"),
                                field("m.m_no").as("mailNumber"),
                                field("m.m_date").as("mailDate"),
                                field("m.m_subject").as("subject"),
                                field("m.m_content").as("content"),
                                field("m.m_note").as("note"),
                                field("m.m_max_response_date").as("maxResponseDate"),
                                field("m.m_status").as("status"),
                                field("m.m_created_by_name").as("createdByName"),
                                field("m.m_created_by").as("createdBy"),
                                field("m.m_to_str").as("toStr"),
                                field("m.m_attachment_qty").as("attachmentQty"),
                                field("m.m_created_date").as("createdDate"),
                                field("m.m_updated_date").as("updatedDate"),
                                field("mt.mail_type").as("mailTypeName"),
                                field("mt.mail_type_id").as("mailTypeId"),
                                field("mc.mcat_name").as("mailCategoryName"),
                                field("mc.mcat_id").as("mailCategoryId"),
                                field("m.m_root_id").as("rootMailId"),
                                field("m.m_parent_id").as("parentMailId"),
                                field("m.m_no_surat_masuk").as("noSuratMasuk"),
                                field("m.m_asal_surat_masuk").as("asalSuratMasuk"),
                                field("m.m_tgl_surat_masuk").as("tglSuratMasuk"),
                                field("m.m_tujuan_surat_keluar").as("tujuanSuratKeluar"),
                                field("m.m_penerima_surat_keluar").as("penerimaSuratKeluar"))
                                .from(table("mail").as("m"))
                                .leftJoin(table("mail_type").as("mt"))
                                .on(field("mt.mail_type_id").eq(field("m.m_type")))
                                .leftJoin(table("mail_category").as("mc"))
                                .on(field("mc.mcat_id").eq(field("m.m_category")))
                                .where(field("m.m_id").eq(id))
                                .and(field("m.m_status").ge(0))
                                .fetchOptional(this::mapToResponse);
        }

        private MailResponse mapToResponse(org.jooq.Record r) {
                return new MailResponse(
                                new MailId(r.get("id", Long.class)),
                                r.get("mailNumber", String.class),
                                r.get("mailDate", LocalDate.class),
                                new MailTypeLookup(
                                                r.get("mailTypeId") != null
                                                                ? new MailTypeId(r.get("mailTypeId", Long.class))
                                                                : null,
                                                r.get("mailTypeName", String.class)),
                                new MailCategoryLookup(
                                                r.get("mailCategoryId") != null
                                                                ? new MailCategoryId(r.get("mailCategoryId", Long.class))
                                                                : null,
                                                r.get("mailCategoryName", String.class)),
                                r.get("subject", String.class),
                                r.get("content", String.class),
                                r.get("note", String.class),
                                r.get("maxResponseDate", LocalDate.class),
                                r.get("status", Integer.class),
                                new MailComponentDto.MailThreadInfoDto(
                                                r.get("rootMailId") != null
                                                                ? new MailId(r.get("rootMailId", Long.class))
                                                                : null,
                                                r.get("parentMailId") != null
                                                                ? new MailId(r.get("parentMailId", Long.class))
                                                                : null),
                                new MailComponentDto.MailSummaryInfoDto(
                                                r.get("attachmentQty", Integer.class),
                                                r.get("toStr", String.class)),
                                new MailComponentDto.MailAuditInfoDto(
                                                r.get("createdBy") != null
                                                                ? new UserId(r.get("createdBy", Long.class))
                                                                : null,
                                                r.get("createdByName", String.class),
                                                r.get("createdDate", LocalDateTime.class),
                                                r.get("updatedDate", LocalDateTime.class)),
                                r.get("noSuratMasuk", String.class),
                                r.get("asalSuratMasuk", String.class),
                                r.get("tglSuratMasuk", LocalDate.class),
                                r.get("tujuanSuratKeluar", String.class),
                                r.get("penerimaSuratKeluar", String.class),
                                null // attachments to be filled by service
                );
        }

        private MailSummaryResponse mapToSummaryResponse(org.jooq.Record r) {
                return new MailSummaryResponse(
                                new MailId(r.get("id", Long.class)),
                                r.get("mailNumber", String.class),
                                r.get("mailDate", LocalDate.class),
                                r.get("subject", String.class),
                                new MailComponentDto.MailAuditInfoDto(
                                                r.get("createdBy") != null
                                                                ? new UserId(r.get("createdBy", Long.class))
                                                                : null,
                                                r.get("createdByName", String.class),
                                                r.get("createdDate", LocalDateTime.class),
                                                null),
                                new MailComponentDto.MailSummaryInfoDto(
                                                r.get("attachmentQty", Integer.class),
                                                r.get("toStr", String.class)),
                                r.get("readStatus", Integer.class),
                                r.get("folderId") != null ? new MailFolderId(r.get("folderId", Long.class)) : null,
                                new MailTypeLookup(null, r.get("mailTypeName", String.class)),
                                new MailCategoryLookup(null, r.get("mailCategoryName", String.class)),
                                r.get("circulationName") != null ? r.get("circulationName", String.class) : null,
                                new MailFolderLookup(
                                                r.get("restoreFolderId") != null
                                                                ? new MailFolderId(r.get("restoreFolderId", Long.class))
                                                                : null,
                                                r.get("restoreFolderName") != null
                                                                ? r.get("restoreFolderName", String.class)
                                                                : null),
                                new MailComponentDto.MailThreadInfoDto(
                                                r.get("rootMailId") != null
                                                                ? new MailId(r.get("rootMailId", Long.class))
                                                                : null,
                                                r.get("parentMailId") != null
                                                                ? new MailId(r.get("parentMailId", Long.class))
                                                                : null),
                                r.get("totalCount") != null ? r.get("totalCount", Long.class) : null);
        }
}
