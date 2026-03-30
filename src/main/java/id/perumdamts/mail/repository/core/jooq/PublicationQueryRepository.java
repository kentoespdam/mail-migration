package id.perumdamts.mail.repository.core.jooq;

import id.perumdamts.mail.dto.core.publication.PublicationDto;
import id.perumdamts.mail.dto.core.publication.PublicationParams;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SortField;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static org.jooq.impl.DSL.*;

@Repository
public class PublicationQueryRepository {

    private final DSLContext dsl;

    public PublicationQueryRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<PublicationDto> findAll(PublicationParams params) {
        Condition condition = field("p.status").ne(inline("DELETED"));

        if (params.getStatus() != null) {
            condition = condition.and(field("p.status").eq(inline(params.getStatus().name())));
        }
        if (params.getKeyword() != null && !params.getKeyword().isBlank()) {
            String kw = "%" + params.getKeyword() + "%";
            condition = condition.and(
                    field("p.judul").likeIgnoreCase(kw)
                            .or(field("p.desk").likeIgnoreCase(kw))
            );
        }
        if (params.getTypeId() != null) {
            condition = condition.and(field("p.type").eq(params.getTypeId()));
        }
        if (params.getStartDate() != null && params.getEndDate() != null) {
            condition = condition.and(field("p.published_date").between(params.getStartDate(), params.getEndDate()));
        }

        SortField<?> sort = params.toSortField();

        return dsl.select(
                        field("p.id").as("id"),
                        field("p.judul").as("title"),
                        field("p.desk").as("description"),
                        field("p.type").as("documentTypeId"),
                        field("jd.jenis_dokumen").as("documentTypeName"),
                        field("p.status").as("status"),
                        field("p.published_date").as("publishedDate"),
                        field("p.file_name").as("fileName"),
                        field("p.file_path").as("filePath"),
                        field("p.file_size").as("fileSize"),
                        field("p.created_by_name").as("createdByName"),
                        field("p.created_by_title").as("createdByTitle"),
                        field("p.created_by_user_id").as("createdByUserId"),
                        field("p.created_at").as("createdAt"),
                        field("p.updated_at").as("updatedAt"),
                        count().over().as("totalCount")
                )
                .from(table("area_publik").as("p"))
                .leftJoin(table("jenis_dokumen").as("jd")).on(field("jd.id").eq(field("p.type")))
                .where(condition)
                .orderBy(sort)
                .limit(params.getSize())
                .offset(params.offset())
                .fetchInto(PublicationDto.class);
    }

    public Optional<PublicationDto> findById(Integer id) {
        var result = dsl.select(
                        field("p.id").as("id"),
                        field("p.judul").as("title"),
                        field("p.desk").as("description"),
                        field("p.type").as("documentTypeId"),
                        field("jd.jenis_dokumen").as("documentTypeName"),
                        field("p.status").as("status"),
                        field("p.published_date").as("publishedDate"),
                        field("p.file_name").as("fileName"),
                        field("p.file_path").as("filePath"),
                        field("p.file_size").as("fileSize"),
                        field("p.created_by_name").as("createdByName"),
                        field("p.created_by_title").as("createdByTitle"),
                        field("p.created_by_user_id").as("createdByUserId"),
                        field("p.created_at").as("createdAt"),
                        field("p.updated_at").as("updatedAt")
                )
                .from(table("area_publik").as("p"))
                .leftJoin(table("jenis_dokumen").as("jd")).on(field("jd.id").eq(field("p.type")))
                .where(field("p.id").eq(id))
                .and(field("p.status").ne(inline("DELETED")))
                .fetchOneInto(PublicationDto.class);

        return Optional.ofNullable(result);
    }
}
