package id.perumdamts.mail.dto.id;

import id.perumdamts.mail.entity.master.AllowedFileType;

public record AllowedFileTypeId(long value) implements SqidId {
    @Override
    public Class<?> getEntityClass() {
        return AllowedFileType.class;
    }
}
