package id.perumdamts.mail.dto.master.mailType;

import id.perumdamts.mail.dto.common.HasSqid;
import id.perumdamts.mail.enums.RecordStatus;
import lombok.Value;

@Value
public class MailTypeResponse implements HasSqid {
    String id;
    String name;
    RecordStatus status;
    long categoryCount;
}
