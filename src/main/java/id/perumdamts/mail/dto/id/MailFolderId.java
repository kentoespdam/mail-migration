package id.perumdamts.mail.dto.id;

import id.perumdamts.mail.entity.core.MailFolder;

public record MailFolderId(long value) implements SqidId {
    @Override
    public Class<?> getEntityClass() {
        return MailFolder.class;
    }
}
