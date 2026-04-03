package id.perumdamts.mail.dto.core.publication;

import id.perumdamts.mail.dto.common.HasSqid;
import id.perumdamts.mail.dto.master.documentType.DocumentTypeLookup;
import lombok.Value;

import java.time.LocalDateTime;

@Value
public class PublicationResponse implements HasSqid {
    String id;
    String title;
    String description;
    DocumentTypeLookup documentType;
    String status;
    LocalDateTime publishedDate;
    String fileName;
    Integer fileSize;
    String createdByName;
    String createdByTitle;
    Integer createdByUserId;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
