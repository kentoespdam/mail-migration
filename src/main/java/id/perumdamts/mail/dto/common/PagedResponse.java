package id.perumdamts.mail.dto.common;

import java.util.List;

/**
 * Generic wrapper untuk hasil query yang terpaginasi.
 *
 * <pre>{@code
 * PagedResponse<MailSummaryResponse> result = PagedResponse.of(
 *         items, request.getPage(), request.getSize(), totalElements
 * );
 * }</pre>
 *
 * @param <T> tipe elemen dalam halaman ini.
 */
public record PagedResponse<T>(
        List<T> content,
        int     page,
        int     size,
        long    totalElements,
        int     totalPages,
        boolean first,
        boolean last
) {
    /**
     * Factory method utama.
     *
     * @param content       daftar elemen halaman ini
     * @param page          nomor halaman saat ini (0-based)
     * @param size          ukuran halaman
     * @param totalElements jumlah total elemen di seluruh halaman
     */
    public static <T> PagedResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = size == 0 ? 1 : (int) Math.ceil((double) totalElements / size);
        return new PagedResponse<>(
                content,
                page,
                size,
                totalElements,
                totalPages,
                page == 0,
                page >= totalPages - 1
        );
    }

    /**
     * Shortcut: ambil {@code totalElements} dari window function JOOQ.
     * <p>
     * Konvensi: setiap baris hasil query menyertakan kolom {@code totalCount}
     * (hasil {@code count().over()}).  Gunakan overload ini agar service tidak
     * perlu query count terpisah.
     *
     * @param content       daftar elemen (sudah di-map dari raw rows)
     * @param request       request pagination yang digunakan
     * @param totalElements nilai totalCount dari baris pertama (0 bila kosong)
     */
    public static <T> PagedResponse<T> of(List<T> content, PageRequest request, long totalElements) {
        return of(content, request.getPage(), request.getSize(), totalElements);
    }

    /** Halaman kosong. */
    public static <T> PagedResponse<T> empty(int page, int size) {
        return new PagedResponse<>(List.of(), page, size, 0L, 0, true, true);
    }
}
