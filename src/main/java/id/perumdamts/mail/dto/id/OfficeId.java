package id.perumdamts.mail.dto.id;

import id.perumdamts.mail.dto.id.marker.Office;

public record OfficeId(long value) implements SqidId {
    @Override
    public Class<?> getEntityClass() {
        return Office.class;
    }
}
