package id.perumdamts.mail.api.dto.mail;

import java.time.LocalDate;

public record MailReportRequest(
        Integer mailTypeId,
        Integer mailCategoryId,
        LocalDate startDate,
        LocalDate endDate
) {}
