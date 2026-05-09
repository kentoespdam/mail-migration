package id.perumdamts.mail.dto.master.allowedFileType;

import id.perumdamts.mail.dto.common.HasSqid;
import id.perumdamts.mail.dto.id.AllowedFileTypeId;
import lombok.Value;

import java.time.LocalDateTime;

@Value
public class AllowedFileTypeDto implements HasSqid {
    AllowedFileTypeId id;
    String context;
    String extension;
    Integer maxSizeMb;
    Boolean isActive;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
