package id.perumdamts.mail.dto.core.mail;

import java.time.LocalDate;

public record MailSearchRequest(
        String keyword,
        Integer mailTypeId,
        Integer mailCategoryId,
        LocalDate startDate,
        LocalDate endDate,
        int page,
        int size
) {
    public MailSearchRequest {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 20;
    }

    public int offset() {
        return page * size;
    }
}
