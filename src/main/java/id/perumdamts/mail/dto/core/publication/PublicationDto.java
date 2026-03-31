package id.perumdamts.mail.dto.core.publication;

import id.perumdamts.mail.dto.common.HasSqid;
import id.perumdamts.mail.dto.master.documentType.DocumentTypeDto;
import lombok.Value;

import java.time.LocalDateTime;

@Value
public class PublicationDto implements HasSqid {
    String id;
    String title;
    String description;
    DocumentTypeDto documentType;
    String status;
    LocalDateTime publishedDate;
    String fileName;
    String filePath;
    Integer fileSize;
    String createdByName;
    String createdByTitle;
    Integer createdByUserId;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    Integer totalCount;
}
