package id.perumdamts.mail.repository.core.jooq;

import id.perumdamts.mail.dto.core.publication.PublicationParams;
import id.perumdamts.mail.dto.core.publication.PublicationResponse;
import id.perumdamts.mail.dto.master.documentType.DocumentTypeLookup;
import id.perumdamts.mail.entity.master.DocumentType;
import id.perumdamts.mail.util.SqidsEncoder;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SortField;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.jooq.impl.DSL.*;

@Repository
@Slf4j
public class PublicationQueryRepository {

    private final DSLContext dsl;
    private final SqidsEncoder encoder;

    public PublicationQueryRepository(DSLContext dsl, SqidsEncoder encoder) {
        this.dsl = dsl;
        this.encoder = encoder;
    }

    public Page<PublicationResponse> findAll(PublicationParams params) {
        Condition condition = field("p.status").ne(inline("DELETED"));

        if (params.getStatus() != null) {
            condition = condition.and(field("p.status").eq(inline(params.getStatus().name())));
        }
        if (params.getKeyword() != null && !params.getKeyword().isBlank()) {
            String kw = "%" + params.getKeyword() + "%";
            condition = condition.and(
                    field("p.title").likeIgnoreCase(kw)
                            .or(field("p.description").likeIgnoreCase(kw)));
        }
        if (params.getTypeId() != null && !params.getTypeId().isBlank()) {
            condition = condition.and(field("p.type").eq(encoder.decode(DocumentType.class, params.getTypeId())));
        }
        if (params.getStartDate() != null && params.getEndDate() != null) {
            condition = condition.and(field("p.published_date").between(params.getStartDate(), params.getEndDate()));
        }

        SortField<?> sort = params.toSortField();

        var records = dsl.select(
                field("p.id"),
                field("p.title"),
                field("p.description"),
                field("p.type"),
                field("jd.jenis_dokumen"),
                field("p.status"),
                field("p.published_date"),
                field("p.original_file_name"),
                field("p.system_file_name"),
                field("p.file_size"),
                field("p.created_by_name"),
                field("p.created_by_title"),
                field("p.created_by_user_id"),
                field("p.created_at"),
                field("p.updated_at"),
                count().over().as("total_count"))
                .from(table("area_publik").as("p"))
                .leftJoin(table("jenis_dokumen").as("jd")).on(field("jd.id").eq(field("p.type")))
                .where(condition)
                .orderBy(sort)
                .limit(params.getSize())
                .offset(params.offset())
                .fetch();

        long total = records.isEmpty() ? 0 : records.getFirst().get(field("total_count"), Long.class);
        List<PublicationResponse> content = records.map(this::toDto).stream()
                .filter(java.util.Objects::nonNull)
                .toList();

        return new PageImpl<>(content, PageRequest.of(params.getPage(), params.getSize()), total);
    }

    public Optional<PublicationResponse> findById(Long id) {
        var result = dsl.select(
                field("p.id"),
                field("p.title"),
                field("p.description"),
                field("p.type"),
                field("jd.jenis_dokumen"),
                field("p.status"),
                field("p.published_date"),
                field("p.original_file_name"),
                field("p.system_file_name"),
                field("p.file_size"),
                field("p.created_by_name"),
                field("p.created_by_title"),
                field("p.created_by_user_id"),
                field("p.created_at"),
                field("p.updated_at"))
                .from(table("area_publik").as("p"))
                .leftJoin(table("jenis_dokumen").as("jd")).on(field("jd.id").eq(field("p.type")))
                .where(field("p.id").eq(id))
                .and(field("p.status").ne(inline("DELETED")))
                .fetchOne(this::toDto);

        return Optional.ofNullable(result);
    }

    public record PublicationFileMeta(String originalFileName, String systemFileName, LocalDateTime createdAt) {
    }

    public Optional<PublicationFileMeta> findFileMeta(Long id) {
        return dsl.select(
                field("p.original_file_name"),
                field("p.system_file_name"),
                field("p.created_at"))
                .from(table("area_publik").as("p"))
                .where(field("p.id").eq(id))
                .and(field("p.status").ne(inline("DELETED")))
                .fetchOptional(r -> new PublicationFileMeta(
                        r.get(field("p.original_file_name"), String.class),
                        r.get(field("p.system_file_name"), String.class),
                        r.get(field("p.created_at"), LocalDateTime.class)
                ));
    }

    private PublicationResponse toDto(Record r) {
        Long id = r.get(field("p.id"), Long.class);
        if (id == null) {
            log.warn("Null id found in publication record, skipping");
            return null;
        }

        Long docTypeId = r.get(field("p.type"), Long.class);
        String docTypeName = r.get(field("jd.jenis_dokumen"), String.class);
        DocumentTypeLookup documentTypeLookup = docTypeId != null
                ? new DocumentTypeLookup(new id.perumdamts.mail.dto.id.DocumentTypeId(docTypeId), docTypeName)
                : null;

        return new PublicationResponse(
                new id.perumdamts.mail.dto.id.PublicationId(id),
                r.get(field("p.title"), String.class),
                r.get(field("p.description"), String.class),
                documentTypeLookup,
                r.get(field("p.status"), String.class),
                r.get(field("p.published_date"), LocalDateTime.class),
                r.get(field("p.original_file_name"), String.class),
                r.get(field("p.file_size"), Integer.class),
                r.get(field("p.created_by_name"), String.class),
                r.get(field("p.created_by_title"), String.class),
                r.get(field("p.created_at"), LocalDateTime.class),
                r.get(field("p.updated_at"), LocalDateTime.class));
    }
}
