package id.perumdamts.mail.dto.master.mailCategory;

import id.perumdamts.mail.dto.common.HasSqid;
import id.perumdamts.mail.dto.id.MailCategoryId;
import lombok.Value;

@Value
public class MailCategoryLookup implements HasSqid {
    MailCategoryId id;
    String name;
}
