package id.perumdamts.mail.api.dto.attachment;

import java.time.LocalDateTime;

public record AttachmentResponse(
        Integer id,
        Integer refType,
        Long refId,
        String originalFilename,
        String fileExt,
        Integer fileSize,
        String docNotes,
        LocalDateTime uploadDate,
        String uploadByName
) {}
