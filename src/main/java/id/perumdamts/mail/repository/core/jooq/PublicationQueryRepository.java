package id.perumdamts.mail.repository.core.jooq;

import id.perumdamts.mail.dto.common.SortParam;
import id.perumdamts.mail.dto.core.publication.PublicationDto;
import id.perumdamts.mail.dto.core.publication.PublicationFilter;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SortField;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.jooq.impl.DSL.*;

@Repository
public class PublicationQueryRepository {

    private final DSLContext dsl;

    private static final Map<String, String> PUB_ALLOWED_SORTS = Map.of(
            "createdAt", "p.created_at",
            "publishedDate", "p.published_date",
            "title", "p.judul"
    );

    private static final String PUB_DEFAULT_SORT = "p.created_at";

    public PublicationQueryRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<PublicationDto> findAll(PublicationFilter filter, int offset, int limit) {
        Condition condition = field("p.status").ne(inline("DELETED"));

        if (filter.status() != null) {
            condition = condition.and(field("p.status").eq(inline(filter.status().name())));
        }
        if (filter.keyword() != null && !filter.keyword().isBlank()) {
            String kw = "%" + filter.keyword() + "%";
            condition = condition.and(
                    field("p.judul").likeIgnoreCase(kw)
                            .or(field("p.desk").likeIgnoreCase(kw))
            );
        }
        if (filter.typeId() != null) {
            condition = condition.and(field("p.type").eq(filter.typeId()));
        }
        if (filter.startDate() != null && filter.endDate() != null) {
            condition = condition.and(field("p.published_date").between(filter.startDate(), filter.endDate()));
        }

        SortField<?> sort = SortParam.resolve(filter.sortBy(), filter.sortDir(), PUB_ALLOWED_SORTS, PUB_DEFAULT_SORT);

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
                .limit(limit)
                .offset(offset)
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
