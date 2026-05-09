package id.perumdamts.mail.dto.core.attachment;

import id.perumdamts.mail.dto.id.AttachmentId;
import java.time.LocalDateTime;

public record AttachmentResponse(
        AttachmentId id,
        Integer refType,
        String refId,
        String originalFilename,
        String fileExt,
        Integer fileSize,
        String docNotes,
        LocalDateTime uploadDate,
        String uploadByName
) {}
