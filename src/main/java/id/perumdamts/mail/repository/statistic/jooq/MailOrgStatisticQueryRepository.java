package id.perumdamts.mail.repository.statistic.jooq;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import java.util.List;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import id.perumdamts.mail.entity.statistic.MailOrgStatistic;

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
