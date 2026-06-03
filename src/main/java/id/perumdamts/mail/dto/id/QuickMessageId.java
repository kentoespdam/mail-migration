package id.perumdamts.mail.dto.id;

import id.perumdamts.mail.entity.master.QuickMessage;

public record QuickMessageId(long value) implements SqidId {
    @Override
    public Class<?> getEntityClass() {
        return QuickMessage.class;
    }
}
