package id.perumdamts.mail.dto.id;

import id.perumdamts.mail.entity.master.MessageTemplate;

public record MessageTemplateId(long value) implements SqidId {
    @Override
    public Class<?> getEntityClass() {
        return MessageTemplate.class;
    }
}
