package id.perumdamts.mail.dto.core.publication;

import java.time.LocalDateTime;

public record PublicationDto(
        Integer id,
        String title,
        String description,
        Integer documentTypeId,
        String documentTypeName,
        String status,
        LocalDateTime publishedDate,
        String fileName,
        String filePath,
        Integer fileSize,
        String createdByName,
        String createdByTitle,
        Integer createdByUserId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Integer totalCount
) {}
