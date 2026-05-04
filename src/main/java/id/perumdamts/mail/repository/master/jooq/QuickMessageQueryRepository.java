package id.perumdamts.mail.repository.master.jooq;

import id.perumdamts.mail.dto.master.quickMessage.QuickMessageParams;
import id.perumdamts.mail.dto.master.quickMessage.QuickMessageResponse;
import id.perumdamts.mail.entity.master.QuickMessage;
import id.perumdamts.mail.util.SqidsEncoder;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static org.jooq.impl.DSL.*;

@Repository
@RequiredArgsConstructor
public class QuickMessageQueryRepository {
    private final DSLContext dsl;
    private final SqidsEncoder encoder;

    public Page<QuickMessageResponse> findAll(QuickMessageParams params) {
        Condition condition = field("ps.is_deleted").eq(0);

        if (params.getSearch() != null && !params.getSearch().isBlank()) {
            condition = condition.and(condition("MATCH(ps.pesan) AGAINST(? IN BOOLEAN MODE)", params.getSearch()));
        }

        if (params.getStatus() != null) {
            condition = condition.and(field("ps.status_new").eq(params.getStatus().name()));
        }

        var records = dsl.select(
                        field("ps.id"),
                        field("ps.pesan"),
                        field("ps.status_new"),
                        count().over().as("total_count"))
                .from(table("pesan_singkat").as("ps"))
                .where(condition)
                .orderBy(params.toSortField())
                .limit(params.getSize())
                .offset(params.offset())
                .fetch();

        long total = records.isEmpty() ? 0 : records.getFirst().get(field("total_count"), Long.class);
        List<QuickMessageResponse> content = records.map(this::mapRecordToResponse);

        return new PageImpl<>(content, PageRequest.of(params.getPage(), params.getSize()), total);
    }

    public Optional<QuickMessageResponse> findById(Long id) {
        QuickMessageResponse response = dsl.select(
                        field("ps.id"),
                        field("ps.pesan"),
                        field("ps.status_new"))
                .from(table("pesan_singkat").as("ps"))
                .where(field("ps.id").eq(id))
                .and(field("ps.is_deleted").eq(0))
                .fetchOne(this::mapRecordToResponse);

        return Optional.ofNullable(response);
    }

    private QuickMessageResponse mapRecordToResponse(Record r) {
        Long id = r.get(field("ps.id"), Long.class);
        return new QuickMessageResponse(
                encoder.encode(QuickMessage.class, id),
                r.get(field("ps.pesan"), String.class));
    }
}
