package id.perumdamts.mail.dto.master.mailType;

import id.perumdamts.mail.dto.common.HasSqid;
import id.perumdamts.mail.dto.id.MailTypeId;
import lombok.Value;

@Value
public class MailTypeMiniResponse implements HasSqid {
    MailTypeId id;
    String name;
}
