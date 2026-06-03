package id.perumdamts.mail.dto.id;

import id.perumdamts.mail.entity.core.Mail;

public record MailId(long value) implements SqidId {
    @Override
    public Class<?> getEntityClass() {
        return Mail.class;
    }
}
