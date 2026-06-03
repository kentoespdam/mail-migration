package id.perumdamts.mail.dto.master.mailType;

import id.perumdamts.mail.dto.common.HasSqid;
import id.perumdamts.mail.dto.id.MailTypeId;
import id.perumdamts.mail.enums.RecordStatus;

public record MailTypeResponse(
    MailTypeId id,
    String name,
    RecordStatus status,
    long categoryCount
) implements HasSqid {
    @Override
    public MailTypeId getId() {
        return id;
    }
}
