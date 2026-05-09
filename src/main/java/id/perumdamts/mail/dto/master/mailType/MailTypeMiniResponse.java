package id.perumdamts.mail.dto.master.mailType;

import id.perumdamts.mail.dto.common.HasSqid;
import id.perumdamts.mail.dto.id.MailTypeId;

public record MailTypeMiniResponse(MailTypeId id, String name) implements HasSqid {
    @Override
    public MailTypeId getId() {
        return id;
    }
}
