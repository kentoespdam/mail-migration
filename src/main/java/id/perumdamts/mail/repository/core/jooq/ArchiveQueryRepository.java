package id.perumdamts.mail.repository.core.jooq;

import id.perumdamts.mail.dto.core.archive.ArchiveReportRequest;
import id.perumdamts.mail.dto.core.archive.ArchiveReportResponse;
import id.perumdamts.mail.dto.core.archive.ArchiveSearchRequest;
import id.perumdamts.mail.dto.core.archive.ArchiveSummaryResponse;
import id.perumdamts.mail.entity.master.MailCategory;
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
public class ArchiveQueryRepository {

    private final DSLContext dsl;
    private final SqidsEncoder encoder;

    public ArchiveQueryRepository(DSLContext dsl, SqidsEncoder encoder) {
        this.dsl = dsl;
        this.encoder = encoder;
    }

    public List<ArchiveSummaryResponse> findForAdmin(ArchiveSearchRequest request, String officeCode) {
        Condition condition = field("a.ma_status").ne(3)
                .and(field("a.ma_office_code").eq(officeCode));

        condition = applyFilters(condition, request);

        SortField<?> sort = request.toSortField();

        return dsl.select(
                field("a.ma_id").as("id"),
                field("a.ma_no").as("archiveNumber"),
                field("a.ma_date").as("archiveDate"),
                field("a.ma_subject").as("subject"),
                field("mc.mcat_name").as("categoryName"),
                field("a.ma_status").as("status"),
                field("a.ma_year").as("year"),
                field("a.ma_office_code").as("officeCode"),
                field("a.ma_attachment_qty").as("attachmentQty"),
                field("a.ma_created_date").as("createdDate"),
                field("a.ma_created_by_name").as("createdByName"),
                count().over().as("totalCount"))
                .from(table("mail_archive").as("a"))
                .leftJoin(table("mail_category").as("mc")).on(field("mc.mcat_id").eq(field("a.ma_category")))
                .where(condition)
                .orderBy(sort)
                .limit(request.getSize())
                .offset(request.offset())
                .fetch(this::mapToSummaryResponse);
    }

    public List<ArchiveSummaryResponse> searchWithAcl(ArchiveSearchRequest request,
            List<Long> positionIds,
            String officeCode) {
        Condition condition = field("a.ma_status").eq(2)
                .and(field("a.ma_office_code").eq(officeCode))
                .and(field("acc.position_id").in(positionIds));

        condition = applyFilters(condition, request);

        SortField<?> sort = request.toSortField();

        return dsl.selectDistinct(
                field("a.ma_id").as("id"),
                field("a.ma_no").as("archiveNumber"),
                field("a.ma_date").as("archiveDate"),
                field("a.ma_subject").as("subject"),
                field("mc.mcat_name").as("categoryName"),
                field("a.ma_status").as("status"),
                field("a.ma_year").as("year"),
                field("a.ma_office_code").as("officeCode"),
                field("a.ma_attachment_qty").as("attachmentQty"),
                field("a.ma_created_date").as("createdDate"),
                field("a.ma_created_by_name").as("createdByName"),
                count().over().as("totalCount"))
                .from(table("mail_archive").as("a"))
                .join(table("mail_archive_access").as("acc"))
                .on(field("acc.mail_archive_id").eq(field("a.ma_id")))
                .leftJoin(table("mail_category").as("mc")).on(field("mc.mcat_id").eq(field("a.ma_category")))
                .where(condition)
                .orderBy(sort)
                .limit(request.getSize())
                .offset(request.offset())
                .fetch(this::mapToSummaryResponse);
    }

    public List<ArchiveReportResponse> getReport(ArchiveReportRequest request, String officeCode) {
        Condition condition = field("a.ma_status").ne(3)
                .and(field("a.ma_office_code").eq(officeCode));

        if (request.getCategoryId() != null && !request.getCategoryId().isBlank()) {
            condition = condition
                    .and(field("a.ma_category").eq(encoder.decode(MailCategory.class, request.getCategoryId())));
        }
        if (request.getYear() != null) {
            condition = condition.and(field("a.ma_year").eq(request.getYear()));
        }
        if (request.getStartDate() != null && request.getEndDate() != null) {
            condition = condition.and(field("a.ma_date").between(request.getStartDate(), request.getEndDate()));
        }

        return dsl.select(
                field("mc.mcat_name").as("categoryName"),
                field("a.ma_year").as("year"),
                count().as("totalArchives"),
                count(case_().when(field("a.ma_status").eq(1), inline(1))).as("totalDraft"),
                count(case_().when(field("a.ma_status").eq(2), inline(1))).as("totalArchived"),
                count().over().as("totalCount"))
                .from(table("mail_archive").as("a"))
                .leftJoin(table("mail_category").as("mc")).on(field("mc.mcat_id").eq(field("a.ma_category")))
                .where(condition)
                .groupBy(field("mc.mcat_name"), field("a.ma_year"))
                .orderBy(request.toSortField())
                .limit(request.getSize())
                .offset(request.offset())
                .fetch(r -> new ArchiveReportResponse(
                        r.get("categoryName", String.class),
                        r.get("year", Short.class),
                        r.get("totalArchives", Long.class),
                        r.get("totalDraft", Long.class),
                        r.get("totalArchived", Long.class),
                        r.get("totalCount", Long.class)));
    }

    private Condition applyFilters(Condition condition, ArchiveSearchRequest request) {
        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            String kw = "%" + request.getKeyword() + "%";
            condition = condition.and(
                    field("a.ma_subject").likeIgnoreCase(kw)
                            .or(field("a.ma_no").likeIgnoreCase(kw))
                            .or(field("a.ma_content").likeIgnoreCase(kw))
                            .or(field("a.ma_keyword_flag").likeIgnoreCase(kw)));
        }
        if (request.getCategoryId() != null && !request.getCategoryId().isBlank()) {
            condition = condition
                    .and(field("a.ma_category").eq(encoder.decode(MailCategory.class, request.getCategoryId())));
        }
        if (request.getYear() != null) {
            condition = condition.and(field("a.ma_year").eq(request.getYear()));
        }
        if (request.getStartDate() != null && request.getEndDate() != null) {
            condition = condition.and(field("a.ma_date").between(request.getStartDate(), request.getEndDate()));
        }
        if (request.getStatus() != null) {
            condition = condition.and(field("a.ma_status").eq(request.getStatus()));
        }
        return condition;
    }

    private ArchiveSummaryResponse mapToSummaryResponse(org.jooq.Record r) {
        return new ArchiveSummaryResponse(
                encoder.encode(id.perumdamts.mail.entity.core.MailArchive.class, r.get("id", Long.class)),
                r.get("archiveNumber", String.class),
                r.get("archiveDate", LocalDate.class),
                r.get("subject", String.class),
                r.get("categoryName", String.class),
                r.get("status", Integer.class),
                r.get("year", Short.class),
                r.get("officeCode", String.class),
                r.get("attachmentQty", Integer.class),
                r.get("createdDate", LocalDateTime.class),
                r.get("createdByName", String.class),
                r.get("totalCount", Long.class));
    }
}
