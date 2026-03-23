package id.perumdamts.mail.api.dto.mail;

public record MailReportResponse(
        String mailTypeName,
        String mailCategoryName,
        long totalMails,
        long totalRead,
        long totalUnread
) {}
