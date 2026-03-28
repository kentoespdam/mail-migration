package id.perumdamts.mail.repository.core.jooq;

import id.perumdamts.mail.dto.core.archive.ArchiveReportRequest;
import id.perumdamts.mail.dto.core.archive.ArchiveReportResponse;
import id.perumdamts.mail.dto.core.archive.ArchiveSearchRequest;
import id.perumdamts.mail.dto.core.archive.ArchiveSummaryResponse;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.jooq.impl.DSL.*;

@Repository
public class ArchiveQueryRepository {

    private final DSLContext dsl;

    public ArchiveQueryRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * Admin view: all archives for this office.
     */
    public List<ArchiveSummaryResponse> findForAdmin(ArchiveSearchRequest request, String officeCode) {
        Condition condition = field("a.ma_status").ne(3)
                .and(field("a.ma_office_code").eq(officeCode));

        condition = applyFilters(condition, request);

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
                        field("a.ma_created_by_name").as("createdByName")
                )
                .from(table("mail_archive").as("a"))
                .leftJoin(table("mail_category").as("mc")).on(field("mc.mcat_id").eq(field("a.ma_category")))
                .where(condition)
                .orderBy(field("a.ma_created_date").desc())
                .limit(request.size())
                .offset(request.offset())
                .fetchInto(ArchiveSummaryResponse.class);
    }

    /**
     * ACL-based search: only archives accessible by given position IDs.
     */
    public List<ArchiveSummaryResponse> searchWithAcl(ArchiveSearchRequest request,
                                                       List<Integer> positionIds,
                                                       String officeCode) {
        Condition condition = field("a.ma_status").eq(2)
                .and(field("a.ma_office_code").eq(officeCode))
                .and(field("acc.position_id").in(positionIds));

        condition = applyFilters(condition, request);

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
                        field("a.ma_created_by_name").as("createdByName")
                )
                .from(table("mail_archive").as("a"))
                .join(table("mail_archive_access").as("acc"))
                    .on(field("acc.mail_archive_id").eq(field("a.ma_id")))
                .leftJoin(table("mail_category").as("mc")).on(field("mc.mcat_id").eq(field("a.ma_category")))
                .where(condition)
                .orderBy(field("a.ma_created_date").desc())
                .limit(request.size())
                .offset(request.offset())
                .fetchInto(ArchiveSummaryResponse.class);
    }

    /**
     * Report: aggregation per year/category (fix B9).
     */
    public List<ArchiveReportResponse> getReport(ArchiveReportRequest request, String officeCode) {
        Condition condition = field("a.ma_status").ne(3)
                .and(field("a.ma_office_code").eq(officeCode));

        if (request.categoryId() != null) {
            condition = condition.and(field("a.ma_category").eq(request.categoryId()));
        }
        if (request.year() != null) {
            condition = condition.and(field("a.ma_year").eq(request.year()));
        }
        if (request.startDate() != null && request.endDate() != null) {
            condition = condition.and(field("a.ma_date").between(request.startDate(), request.endDate()));
        }

        return dsl.select(
                        field("mc.mcat_name").as("categoryName"),
                        field("a.ma_year").as("year"),
                        count().as("totalArchives"),
                        count(case_().when(field("a.ma_status").eq(1), inline(1))).as("totalDraft"),
                        count(case_().when(field("a.ma_status").eq(2), inline(1))).as("totalArchived")
                )
                .from(table("mail_archive").as("a"))
                .leftJoin(table("mail_category").as("mc")).on(field("mc.mcat_id").eq(field("a.ma_category")))
                .where(condition)
                .groupBy(field("mc.mcat_name"), field("a.ma_year"))
                .orderBy(field("a.ma_year").desc(), field("mc.mcat_name"))
                .fetchInto(ArchiveReportResponse.class);
    }

    private Condition applyFilters(Condition condition, ArchiveSearchRequest request) {
        if (request.keyword() != null && !request.keyword().isBlank()) {
            String kw = "%" + request.keyword() + "%";
            condition = condition.and(
                    field("a.ma_subject").likeIgnoreCase(kw)
                            .or(field("a.ma_no").likeIgnoreCase(kw))
                            .or(field("a.ma_content").likeIgnoreCase(kw))
                            .or(field("a.ma_keyword_flag").likeIgnoreCase(kw))
            );
        }
        if (request.categoryId() != null) {
            condition = condition.and(field("a.ma_category").eq(request.categoryId()));
        }
        if (request.year() != null) {
            condition = condition.and(field("a.ma_year").eq(request.year()));
        }
        if (request.startDate() != null && request.endDate() != null) {
            condition = condition.and(field("a.ma_date").between(request.startDate(), request.endDate()));
        }
        return condition;
    }
}
