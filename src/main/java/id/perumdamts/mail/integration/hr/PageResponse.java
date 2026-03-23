package id.perumdamts.mail.integration.hr;

import java.util.List;

/**
 * Generic page response dari HR Service.
 */
public record PageResponse<T>(
        List<T> content,
        int totalPages,
        long totalElements,
        int number,
        int size
) {}
