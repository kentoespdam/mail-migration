package id.perumdamts.mail.dto.master.documentType;

import id.perumdamts.mail.dto.common.HasSqid;
import id.perumdamts.mail.dto.id.DocumentTypeId;
import lombok.Value;

@Value
public class DocumentTypeLookup implements HasSqid {
    DocumentTypeId id;
    String name;
}
