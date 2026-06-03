package id.perumdamts.mail.dto.core.attachment;

import java.time.LocalDateTime;

public record AttachmentDetailResponse(
        String id,
        String originalFilename,
        String fileExt,
        Integer fileSize,
        String docNotes,
        LocalDateTime uploadDate,
        String uploadByName
) {}
