package id.perumdamts.mail.dto.id;

import id.perumdamts.mail.entity.core.MailRecipient;

public record MailRecipientId(long value) implements SqidId {
    @Override
    public Class<?> getEntityClass() {
        return MailRecipient.class;
    }
}
