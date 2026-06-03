package id.perumdamts.mail.dto.core.mail;

import id.perumdamts.mail.dto.id.MailId;
import id.perumdamts.mail.dto.id.UserId;

import java.time.LocalDateTime;

public class MailComponentDto {

    public record MailThreadInfoDto(MailId rootMailId, MailId parentMailId) {
    }

    public record MailAuditInfoDto(
            UserId createdBy,
            String createdByName,
            LocalDateTime createdDate,
            LocalDateTime updatedDate) {
    }

    public record MailSummaryInfoDto(Integer attachmentQty, String toStr) {
    }
}
