package id.perumdamts.mail.dto.master.mailType;

import id.perumdamts.mail.dto.common.HasSqid;
import lombok.Value;

@Value
public class MailTypeLookup implements HasSqid {
    String id;
    String name;
}
