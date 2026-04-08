package id.perumdamts.mail.dto.master.mailCategory;

import id.perumdamts.mail.dto.common.HasSqid;
import lombok.Value;

@Value
public class MailCategoryLookup implements HasSqid {
    String id;
    String name;
}
