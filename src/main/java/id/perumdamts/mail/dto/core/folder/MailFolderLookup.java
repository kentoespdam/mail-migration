package id.perumdamts.mail.dto.core.folder;

import id.perumdamts.mail.dto.common.HasSqid;
import lombok.Value;

@Value
public class MailFolderLookup implements HasSqid {
    String id;
    String name;
}
