package id.perumdamts.mail.dto.master.documentType;

import id.perumdamts.mail.dto.common.HasSqid;
import lombok.Value;

@Value
public class DocumentTypeLookup implements HasSqid {
    String id;
    String name;
}
