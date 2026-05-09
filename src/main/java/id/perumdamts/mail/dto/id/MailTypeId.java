package id.perumdamts.mail.dto.id;

import id.perumdamts.mail.entity.master.MailType;

public record MailTypeId(long value) implements SqidId {
    @Override
    public Class<?> getEntityClass() {
        return MailType.class;
    }
}
