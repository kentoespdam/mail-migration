package id.perumdamts.mail.dto.master.documentType;

import id.perumdamts.mail.dto.common.HasSqid;
import id.perumdamts.mail.dto.id.DocumentTypeId;
import id.perumdamts.mail.enums.RecordStatusActive;
import lombok.Value;

@Value
public class DocumentTypeResponse implements HasSqid {
    DocumentTypeId id;
    String name;
    RecordStatusActive status;
    long publicationCount;
}
