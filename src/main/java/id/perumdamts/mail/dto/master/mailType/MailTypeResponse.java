package id.perumdamts.mail.dto.master.mailType;

import id.perumdamts.mail.dto.common.HasSqid;
import id.perumdamts.mail.dto.id.MailTypeId;
import id.perumdamts.mail.enums.RecordStatus;
import lombok.Value;

@Value
public class MailTypeResponse implements HasSqid {
    MailTypeId id;
    String name;
    RecordStatus status;
    long categoryCount;
}
