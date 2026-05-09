package id.perumdamts.mail.dto.id;

import id.perumdamts.mail.dto.id.marker.User;

public record UserId(long value) implements SqidId {
    @Override
    public Class<?> getEntityClass() {
        return User.class;
    }
}
