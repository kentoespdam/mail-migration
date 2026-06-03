package id.perumdamts.mail.dto.id;

import id.perumdamts.mail.dto.id.marker.Position;

public record PositionId(long value) implements SqidId {
    @Override
    public Class<?> getEntityClass() {
        return Position.class;
    }
}
