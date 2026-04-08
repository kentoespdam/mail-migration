package id.perumdamts.mail.dto.core.mail;

import lombok.Value;

@Value
public class MailReportResponse {
        String mailTypeName;
        String mailCategoryName;
        long totalMails;
        long totalRead;
        long totalUnread;
        long totalCount;
}
