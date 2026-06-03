package id.perumdamts.mail.dto.id;

import id.perumdamts.mail.entity.core.Publication;

public record PublicationId(long value) implements SqidId {
    @Override
    public Class<?> getEntityClass() {
        return Publication.class;
    }
}
