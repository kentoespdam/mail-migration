package id.perumdamts.mail.dto.core.archive;

import java.time.LocalDateTime;

public record MailArchiveNotifResponse(
    Integer id,
    String mailArchiveId,
    Integer notifFlag,
    LocalDateTime insertDate,
    LocalDateTime processedDate,
    LocalDateTime updatedAt
) {}
