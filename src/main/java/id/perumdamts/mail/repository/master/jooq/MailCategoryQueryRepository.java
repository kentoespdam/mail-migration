package id.perumdamts.mail.repository.master.jooq;

import id.perumdamts.mail.dto.master.mailCategory.MailCategoryParams;
import id.perumdamts.mail.dto.master.mailCategory.MailCategoryResponse;
import id.perumdamts.mail.dto.master.mailType.MailTypeMiniResponse;
import id.perumdamts.mail.entity.master.MailCategory;
import id.perumdamts.mail.entity.master.MailType;
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
public class MailCategoryQueryRepository {
    private final DSLContext dsl;
    private final SqidsEncoder encoder;

    public Page<MailCategoryResponse> findAll(MailCategoryParams params) {
        Condition condition = field("mc.mcat_status").ne(inline("Deleted"));

        if (params.getSearch() != null && !params.getSearch().isBlank()) {
            condition = condition.and(
                    field("mc.mcat_name").likeIgnoreCase("%" + params.getSearch() + "%")
                            .or(field("mc.mcat_code").likeIgnoreCase("%" + params.getSearch() + "%"))
            );
        }

        if (params.getMailTypeId() != null) {
            condition = condition.and(field("mc.mail_type_id").eq(params.getMailTypeId()));
        }

        var records = dsl.select(
                        field("mc.mcat_id"),
                        field("mc.mcat_code"),
                        field("mc.mcat_name"),
                        field("mc.mcat_status"),
                        field("mc.sort"),
                        field("mt.mail_type_id"),
                        field("mt.mail_type"),
                        count().over().as("total_count")
                )
                .from(table("mail_category").as("mc"))
                .join(table("mail_type").as("mt")).on(field("mc.mail_type_id").eq(field("mt.mail_type_id")))
                .where(condition)
                .orderBy(params.toSortField())
                .limit(params.getSize())
                .offset(params.offset())
                .fetch();

        long total = records.isEmpty() ? 0 : records.getFirst().get(field("total_count"), Long.class);
        List<MailCategoryResponse> content = records.map(this::mapRecordToResponse);

        return new PageImpl<>(content, PageRequest.of(params.getPage(), params.getSize()), total);
    }

    public Optional<MailCategoryResponse> findById(Long id) {
        MailCategoryResponse response = dsl.select(
                        field("mc.mcat_id"),
                        field("mc.mcat_code"),
                        field("mc.mcat_name"),
                        field("mc.mcat_status"),
                        field("mc.sort"),
                        field("mt.mail_type_id"),
                        field("mt.mail_type")
                )
                .from(table("mail_category").as("mc"))
                .join(table("mail_type").as("mt")).on(field("mc.mail_type_id").eq(field("mt.mail_type_id")))
                .where(field("mc.mcat_id").eq(id))
                .and(field("mc.mcat_status").ne(inline("Deleted")))
                .fetchOne(this::mapRecordToResponse);

        return Optional.ofNullable(response);
    }

    private MailCategoryResponse mapRecordToResponse(Record r) {
        Long mcatId = r.get(field("mc.mcat_id"), Long.class);
        Long mailTypeId = r.get(field("mt.mail_type_id"), Long.class);
        String code = r.get(field("mc.mcat_code"), String.class);
        String name = r.get(field("mc.mcat_name"), String.class);
        String status = r.get(field("mc.mcat_status"), String.class);
        Integer sort = r.get(field("mc.sort"), Integer.class);

        return new MailCategoryResponse(
                encoder.encode(MailCategory.class, mcatId),
                new MailTypeMiniResponse(encoder.encode(MailType.class, mailTypeId), r.get(field("mt.mail_type"), String.class)),
                code,
                name,
                code + " - " + name,
                status,
                sort
        );
    }
}
