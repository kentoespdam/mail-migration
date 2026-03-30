package id.perumdamts.mail.dto.common;

import lombok.Getter;
import lombok.Setter;
import org.jooq.SortField;

import java.util.Map;

/**
 * Base class untuk request yang membutuhkan <b>pagination + sorting</b>.
 * <p>
 * Extend class ini pada setiap search/index request DTO.
 *
 * <pre>{@code
 * public class MailSearchRequest extends PagedRequest {
 *
 *     private static final Map<String, String> ALLOWED_SORTS = Map.of(
 *             "createdDate", "m.m_created_date",
 *             "subject",     "m.m_subject"
 *     );
 *     private static final String DEFAULT_SORT = "m.m_created_date";
 *
 *     private String keyword;
 *
 *     @Override
 *     protected Map<String, String> allowedSorts()  { return ALLOWED_SORTS; }
 *
 *     @Override
 *     protected String defaultSortColumn()           { return DEFAULT_SORT; }
 * }
 * }</pre>
 */
@Setter
@Getter
public abstract class PagedRequest extends PageRequest {

    protected PagedRequest() {
        super();
    }

    protected PagedRequest(int page, int size) {
        super(page, size);
    }

    // ── Sort properties ──────────────────────────────────────────────────────

    private String sortBy;
    private String sortDir;

    // ── Template methods ─────────────────────────────────────────────────────

    /**
     * Whitelist kolom yang boleh digunakan untuk sorting.
     * Key  = nama field dari client (camelCase).
     * Value = kolom SQL (dengan alias tabel bila perlu).
     */
    protected abstract Map<String, String> allowedSorts();

    /**
     * Kolom SQL default bila {@link #getSortBy()} tidak ada di whitelist.
     */
    protected abstract String defaultSortColumn();

    // ── Helper ───────────────────────────────────────────────────────────────

    /**
     * Resolve {@link SortField} yang siap dipakai di query JOOQ.
     *
     * <pre>{@code
     * dsl.selectFrom(TABLE)
     *    .orderBy(request.toSortField())
     *    ...
     * }</pre>
     */
    public SortField<?> toSortField() {
        return SortParam.resolve(sortBy, sortDir, allowedSorts(), defaultSortColumn());
    }
}
