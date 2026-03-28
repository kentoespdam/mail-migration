package id.perumdamts.mail.dto.core.publication;

import java.time.LocalDateTime;

public record AllowedFileTypeDto(
        Integer id,
        String context,
        String extension,
        Integer maxSizeMb,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
