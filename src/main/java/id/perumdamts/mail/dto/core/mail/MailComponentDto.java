package id.perumdamts.mail.dto.core.mail;

import java.time.LocalDateTime;

public class MailComponentDto {

    public record MailThreadInfoDto(String rootMailId, String parentMailId) {
    }

    public record MailAuditInfoDto(
            String createdBy,
            String createdByName,
            LocalDateTime createdDate,
            LocalDateTime updatedDate) {
    }

    public record MailSummaryInfoDto(Integer attachmentQty, String toStr) {
    }
}
