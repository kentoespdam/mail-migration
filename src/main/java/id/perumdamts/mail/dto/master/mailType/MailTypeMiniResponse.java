package id.perumdamts.mail.dto.master.mailType;

import id.perumdamts.mail.dto.common.HasSqid;
import lombok.Value;

@Value
public class MailTypeMiniResponse implements HasSqid {
    String id;
    String name;
}
