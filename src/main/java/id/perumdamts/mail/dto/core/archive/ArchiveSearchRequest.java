package id.perumdamts.mail.dto.core.archive;

import id.perumdamts.mail.dto.common.PagedRequest;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Map;

/**
 * Request pencarian arsip dengan pagination dan sorting.
 */
@Setter
@Getter
public class ArchiveSearchRequest extends PagedRequest {

    private static final Map<String, String> ALLOWED_SORTS = Map.of(
            "createdDate",   "a.created_date",
            "archiveDate",   "a.archive_date",
            "archiveNumber", "a.archive_no",
            "subject",       "a.subject"
    );
    private static final String DEFAULT_SORT = "a.created_date";

    // ── Filter fields ─────────────────────────────────────────────────────────

    private String    keyword;
    private Integer   categoryId;
    private Short     year;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer   status;

    // ── Constructors ──────────────────────────────────────────────────────────

    public ArchiveSearchRequest() {
        super();
    }

    // ── Template methods ──────────────────────────────────────────────────────

    @Override
    protected Map<String, String> allowedSorts()  { return ALLOWED_SORTS; }

    @Override
    protected String defaultSortColumn()           { return DEFAULT_SORT; }

    // ── Getters / Setters ─────────────────────────────────────────────────────

}
