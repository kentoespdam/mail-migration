package id.perumdamts.mail.repository.statistic.jooq;

import id.perumdamts.mail.entity.statistic.MailOrgStatistic;
import org.jooq.DSLContext;
import org.jooq.Record4;
import org.jooq.Result;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static org.jooq.impl.DSL.table;
import static org.jooq.impl.DSL.field;

@Repository
public class MailOrgStatisticQueryRepository {
    private final DSLContext dsl;

    public MailOrgStatisticQueryRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<MailOrgStatistic> findByPeriodMonth(int periodMonth) {
        return dsl.selectFrom(table("mail_org_statistic"))
                .where(field("period_month").eq(periodMonth))
                .fetchInto(MailOrgStatistic.class);
    }
}
