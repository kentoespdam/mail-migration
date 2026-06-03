package id.perumdamts.mail.dto.id;

import id.perumdamts.mail.enums.CirculationType;

public record CirculationTypeId(long value) implements SqidId {
    @Override
    public Class<?> getEntityClass() {
        return CirculationType.class;
    }
}
