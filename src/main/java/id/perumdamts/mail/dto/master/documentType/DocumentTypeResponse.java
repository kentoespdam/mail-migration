package id.perumdamts.mail.dto.master.documentType;

import id.perumdamts.mail.dto.common.HasSqid;
import id.perumdamts.mail.enums.RecordStatus;
import lombok.Value;

@Value
public class DocumentTypeResponse implements HasSqid {
    String id;
    String name;
    RecordStatus status;
    long publicationCount;
}
