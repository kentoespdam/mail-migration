package id.perumdamts.mail.dto.core.archive;

import java.time.LocalDateTime;

public record ArchiveAccessResponse(
        Integer id,
        Long archiveId,
        Integer positionId,
        Boolean canAccess,
        Boolean canDownload,
        Boolean canViewHistory
) {}
