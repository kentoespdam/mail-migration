package id.perumdamts.mail.repository.respontime.jooq;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
public class MailResponseTimeQueryRepository {

    private final DSLContext dsl;

    public MailResponseTimeQueryRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    // TODO: Add query methods for response time reporting if needed
}
