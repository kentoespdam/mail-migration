package id.perumdamts.mail.dto.core.mail;

import java.time.LocalDate;

public record MailReportRequest(
        Integer mailTypeId,
        Integer mailCategoryId,
        LocalDate startDate,
        LocalDate endDate
) {}
