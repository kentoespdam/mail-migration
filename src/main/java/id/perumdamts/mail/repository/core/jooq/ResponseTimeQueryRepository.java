package id.perumdamts.mail.repository.core.jooq;

import id.perumdamts.mail.dto.core.response.ResponseTimeFilterRequest;
import id.perumdamts.mail.dto.core.response.ResponseTimeStatsDto;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import static org.jooq.impl.DSL.*;

@Repository
public class ResponseTimeQueryRepository {

    private final DSLContext dsl;

    public ResponseTimeQueryRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public ResponseTimeStatsDto aggregate(ResponseTimeFilterRequest filter) {
        var whereClause = buildWhereClause(filter);

        Record record = dsl.select(
                        count(field("rt.respon_time")).cast(Long.class).as("count"),
                        avg(field("rt.respon_time").cast(Double.class)).as("avg"),
                        min(field("rt.respon_time").cast(Double.class)).as("min"),
                        max(field("rt.respon_time").cast(Double.class)).as("max")
                )
                .from(table("mail_respontime").as("rt"))
                .leftJoin(table("mail").as("reply")).on(field("reply.m_id").eq(field("rt.reply_m_id")))
                .where(whereClause)
                .fetchOne();

        if (record == null) {
            return ResponseTimeStatsDto.empty();
        }

        Long count = record.get("count", Long.class);
        if (count == null || count == 0) {
            return ResponseTimeStatsDto.empty();
        }

        Double avg = record.get("avg", Double.class);
        Double min = record.get("min", Double.class);
        Double max = record.get("max", Double.class);

        Double p50 = calculatePercentile(filter, 0.50);
        Double p90 = calculatePercentile(filter, 0.90);
        Double p99 = calculatePercentile(filter, 0.99);

        return new ResponseTimeStatsDto(
                count,
                avg != null ? avg : 0.0,
                p50 != null ? p50 : avg,
                p90 != null ? p90 : avg,
                p99 != null ? p99 : avg,
                min != null ? min : 0.0,
                max != null ? max : 0.0
        );
    }

    private Double calculatePercentile(ResponseTimeFilterRequest filter, double percentile) {
        var whereClause = buildWhereClause(filter);

        Record countRecord = dsl.select(
                        count(field("rt.respon_time")).cast(Long.class).as("cnt")
                )
                .from(table("mail_respontime").as("rt"))
                .leftJoin(table("mail").as("reply")).on(field("reply.m_id").eq(field("rt.reply_m_id")))
                .where(whereClause)
                .fetchOne();

        if (countRecord == null) return null;

        Long totalCount = countRecord.get("cnt", Long.class);
        if (totalCount == null || totalCount == 0) return null;

        int position = (int) Math.ceil(percentile * totalCount) - 1;
        if (position < 0) position = 0;

        Record percentileRecord = dsl.select(
                        field("rt.respon_time").cast(Double.class).as("respon_time")
                )
                .from(table("mail_respontime").as("rt"))
                .leftJoin(table("mail").as("reply")).on(field("reply.m_id").eq(field("rt.reply_m_id")))
                .where(whereClause)
                .orderBy(field("rt.respon_time"))
                .limit(1)
                .offset(position)
                .fetchOne();

        return percentileRecord != null ? percentileRecord.get("respon_time", Double.class) : null;
    }

    private org.jooq.Condition buildWhereClause(ResponseTimeFilterRequest filter) {
        var condition = field("rt.respon_time").isNotNull();

        if (filter.mailTypeId() != null) {
            condition = condition.and(field("rt.m_type").eq(filter.mailTypeId()));
        }

        if (filter.mailCategoryId() != null) {
            condition = condition.and(field("rt.m_category").eq(filter.mailCategoryId()));
        }

        if (filter.startDate() != null) {
            condition = condition.and(field("rt.orig_date").greaterOrEqual(filter.startDate().atStartOfDay()));
        }

        if (filter.endDate() != null) {
            condition = condition.and(field("rt.orig_date").lessOrEqual(filter.endDate().plusDays(1).atStartOfDay()));
        }

        if (filter.unitId() != null) {
            condition = condition.and(field("reply.m_created_by").eq(filter.unitId()));
        }

        return condition;
    }
}