package id.perumdamts.mail.dto.core.mail;

import id.perumdamts.mail.dto.common.PagedRequest;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Map;

/**
 * Request pencarian surat masuk/keluar dengan pagination dan sorting.
 */
@Setter
@Getter
public class MailSearchRequest extends PagedRequest {

    private static final Map<String, String> ALLOWED_SORTS = Map.of(
            "createdDate", "m.m_created_date",
            "mailDate", "m.m_date",
            "subject", "m.m_subject",
            "mailNumber", "m.m_no");
    private static final String DEFAULT_SORT = "m.m_created_date";

    // ── Filter fields ─────────────────────────────────────────────────────────

    private String keyword;
    private String mailTypeId;
    private String mailCategoryId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean hasAttachment;
    private String senderId;

    // ── Constructors ──────────────────────────────────────────────────────────

    public MailSearchRequest() {
        super();
    }

    // ── Template methods ──────────────────────────────────────────────────────

    @Override
    protected Map<String, String> allowedSorts() {
        return ALLOWED_SORTS;
    }

    @Override
    protected String defaultSortColumn() {
        return DEFAULT_SORT;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

}
