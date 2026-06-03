package id.perumdamts.mail.dto.id;

import id.perumdamts.mail.entity.master.MailCategory;

public record MailCategoryId(long value) implements SqidId {
    @Override
    public Class<?> getEntityClass() {
        return MailCategory.class;
    }
}
