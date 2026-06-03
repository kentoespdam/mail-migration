package id.perumdamts.mail.dto.core.folder;

import id.perumdamts.mail.dto.common.HasSqid;
import id.perumdamts.mail.dto.id.MailFolderId;
import lombok.Value;

@Value
public class MailFolderLookup implements HasSqid {
    MailFolderId id;
    String name;
}
