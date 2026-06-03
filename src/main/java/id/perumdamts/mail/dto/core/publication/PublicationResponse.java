package id.perumdamts.mail.dto.core.publication;

import id.perumdamts.mail.dto.common.HasSqid;
import id.perumdamts.mail.dto.master.documentType.DocumentTypeLookup;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor(force = true, access = lombok.AccessLevel.PRIVATE)
public class PublicationResponse implements HasSqid {
    String id;
    String title;
    String description;
    DocumentTypeLookup documentType;
    String status;
    LocalDateTime publishedDate;
    String originalFileName;
    Integer fileSize;
    String createdByName;
    String createdByTitle;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
