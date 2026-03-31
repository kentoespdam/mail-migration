package id.perumdamts.mail.dto.common;

import id.perumdamts.mail.entity.SqidEntity;
import id.perumdamts.mail.util.SqidsEncoder;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class SqidMapper<E extends SqidEntity> {

    @Autowired
    private SqidsEncoder encoder;

    protected String sqid(E entity) {
        return encoder.encode(entity.getClass(), entity.getId());
    }
}
