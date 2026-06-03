package id.perumdamts.mail.dto.common;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;

/**
 * Base class untuk request yang membutuhkan pagination.
 * <p>
 * Extend class ini pada setiap request DTO yang memerlukan halaman (page/size).
 * Gunakan {@link PagedRequest} bila request juga butuh sorting.
 *
 * <pre>{@code
 * public class MailSearchRequest extends PageRequest {
 *     private String keyword;
 *     // ...
 * }
 * }</pre>
 */
@Getter
public abstract class PageRequest {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    @Min(0)
    private int page;

    @Min(1)
    @Max(MAX_SIZE)
    private int size;

    public PageRequest() {
        this.page = 0;
        this.size = DEFAULT_SIZE;
    }

    protected PageRequest(int page, int size) {
        this.page = Math.max(page, 0);
        this.size = (size <= 0 || size > MAX_SIZE) ? DEFAULT_SIZE : size;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    /**
     * Offset SQL untuk JOOQ / JDBC: {@code page * size}.
     */
    public int offset() {
        return page * size;
    }

    // ── Setters (untuk deserialisasi Jackson / Spring MVC @ModelAttribute) ──

    public void setPage(int page) {
        this.page = Math.max(page, 0);
    }

    public void setSize(int size) {
        this.size = (size <= 0 || size > MAX_SIZE) ? DEFAULT_SIZE : size;
    }
}
