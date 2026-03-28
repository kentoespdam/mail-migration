package id.perumdamts.mail.dto.core.mail;

public record MailReportResponse(
        String mailTypeName,
        String mailCategoryName,
        long totalMails,
        long totalRead,
        long totalUnread
) {}
