package id.perumdamts.mail.dto.id;

import id.perumdamts.mail.entity.master.DocumentType;

public record DocumentTypeId(long value) implements SqidId {
    @Override
    public Class<?> getEntityClass() {
        return DocumentType.class;
    }
}
