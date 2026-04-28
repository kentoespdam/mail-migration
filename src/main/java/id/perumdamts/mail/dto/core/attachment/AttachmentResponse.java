package id.perumdamts.mail.dto.core.attachment;

import java.time.LocalDateTime;

public record AttachmentResponse(
        String id,
        Integer refType,
        String refId,
        String originalFilename,
        String fileExt,
        Integer fileSize,
        String docNotes,
        LocalDateTime uploadDate,
        String uploadByName
) {}
