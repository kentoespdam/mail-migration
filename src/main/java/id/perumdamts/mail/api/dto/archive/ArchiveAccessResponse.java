package id.perumdamts.mail.api.dto.archive;

import java.time.LocalDateTime;

public record ArchiveAccessResponse(
        Integer id,
        Long archiveId,
        Integer positionId,
        Integer accessLevel,
        LocalDateTime grantedDate,
        Integer grantedBy
) {}
