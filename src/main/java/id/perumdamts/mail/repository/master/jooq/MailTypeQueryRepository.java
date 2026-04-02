package id.perumdamts.mail.repository.master.jooq;

import id.perumdamts.mail.dto.master.mailType.MailTypeParams;
import id.perumdamts.mail.dto.master.mailType.MailTypeResponse;
import id.perumdamts.mail.entity.master.MailType;
import id.perumdamts.mail.enums.RecordStatus;
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
public class MailTypeQueryRepository {
    private final DSLContext dsl;
    private final SqidsEncoder encoder;

    public Page<MailTypeResponse> findAll(MailTypeParams params) {
        Condition condition = field("mt.mail_type_status").ne(inline("DELETED"));

        if (params.getSearch() != null && !params.getSearch().isBlank()) {
            condition = condition.and(field("mt.mail_type").likeIgnoreCase("%" + params.getSearch() + "%"));
        }

        var records = dsl.select(
                        field("mt.mail_type_id"),
                        field("mt.mail_type"),
                        field("mt.mail_type_status"),
                        field(
                                dsl.selectCount()
                                        .from(table("mail_category").as("mc"))
                                        .where(field("mc.mail_type_id").eq(field("mt.mail_type_id")))
                                        .and(field("mc.mcat_status").ne(inline("Deleted")))
                        ).as("category_count"),
                        count().over().as("total_count")
                )
                .from(table("mail_type").as("mt"))
                .where(condition)
                .orderBy(params.toSortField())
                .limit(params.getSize())
                .offset(params.offset())
                .fetch();

        long total = records.isEmpty() ? 0 : records.getFirst().get(field("total_count"), Long.class);
        List<MailTypeResponse> content = records.map(this::mapRecordToResponse);

        return new PageImpl<>(content, PageRequest.of(params.getPage(), params.getSize()), total);
    }

    public Optional<MailTypeResponse> findById(Long id) {
        MailTypeResponse response = dsl.select(
                        field("mt.mail_type_id"),
                        field("mt.mail_type"),
                        field("mt.mail_type_status"),
                        field(
                                dsl.selectCount()
                                        .from(table("mail_category").as("mc"))
                                        .where(field("mc.mail_type_id").eq(field("mt.mail_type_id")))
                                        .and(field("mc.mcat_status").ne(inline("Deleted")))
                        ).as("category_count")
                )
                .from(table("mail_type").as("mt"))
                .where(field("mt.mail_type_id").eq(id))
                .and(field("mt.mail_type_status").ne(inline("DELETED")))
                .fetchOne(this::mapRecordToResponse);

        return Optional.ofNullable(response);
    }

    private MailTypeResponse mapRecordToResponse(Record r) {
        Long id = r.get(field("mt.mail_type_id"), Long.class);
        return new MailTypeResponse(
                encoder.encode(MailType.class, id),
                r.get(field("mt.mail_type"), String.class),
                RecordStatus.valueOf(r.get(field("mt.mail_type_status"), String.class)),
                r.get(field("category_count"), Long.class)
        );
    }
}

