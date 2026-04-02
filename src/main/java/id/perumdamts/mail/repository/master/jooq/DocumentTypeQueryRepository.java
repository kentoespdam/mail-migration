package id.perumdamts.mail.repository.master.jooq;

import id.perumdamts.mail.dto.master.documentType.DocumentTypeParams;
import id.perumdamts.mail.dto.master.documentType.DocumentTypeResponse;
import id.perumdamts.mail.entity.master.DocumentType;
import id.perumdamts.mail.enums.RecordStatus;
import id.perumdamts.mail.util.SqidsEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class DocumentTypeQueryRepository {

    private final DSLContext dsl;
    private final SqidsEncoder encoder;

    public Page<DocumentTypeResponse> findAll(DocumentTypeParams params) {
        Condition condition = field("jd.status").ne(inline("DELETED"));

        if (params.getSearch() != null && !params.getSearch().isBlank()) {
            condition = condition.and(field("jd.jenis_dokumen").likeIgnoreCase("%" + params.getSearch() + "%"));
        }

        var records = dsl.select(
                        field("jd.id"),
                        field("jd.jenis_dokumen"),
                        field("jd.status"),
                        field(
                                dsl.selectCount()
                                        .from(table("area_publik").as("p"))
                                        .where(field("p.type").eq(field("jd.id")))
                                        .and(field("p.status").ne(inline("DELETED")))
                        ).as("publication_count"),
                        count().over().as("total_count")
                )
                .from(table("jenis_dokumen").as("jd"))
                .where(condition)
                .orderBy(params.toSortField())
                .limit(params.getSize())
                .offset(params.offset())
                .fetch();

        long total = records.isEmpty() ? 0 : records.getFirst().get(field("total_count"), Long.class);
        List<DocumentTypeResponse> content = records.map(this::mapRecordToResponse);

        return new PageImpl<>(content, PageRequest.of(params.getPage(), params.getSize()), total);
    }

    public Optional<DocumentTypeResponse> findById(Long id) {
        DocumentTypeResponse response = dsl.select(
                        field("jd.id"),
                        field("jd.jenis_dokumen"),
                        field("jd.status"),
                        field(
                                dsl.selectCount()
                                        .from(table("area_publik").as("p"))
                                        .where(field("p.type").eq(field("jd.id")))
                                        .and(field("p.status").ne(inline("DELETED")))
                        ).as("publication_count")
                )
                .from(table("jenis_dokumen").as("jd"))
                .where(field("jd.id").eq(id))
                .and(field("jd.status").ne(inline("DELETED")))
                .fetchOne(this::mapRecordToResponse);

        return Optional.ofNullable(response);
    }

    private DocumentTypeResponse mapRecordToResponse(Record r) {
        Long docId = r.get(field("jd.id"), Long.class);
        return new DocumentTypeResponse(
                encoder.encode(DocumentType.class, docId),
                r.get(field("jd.jenis_dokumen"), String.class),
                RecordStatus.valueOf(r.get(field("jd.status"), String.class)),
                r.get(field("publication_count"), Long.class)
        );
    }
}
