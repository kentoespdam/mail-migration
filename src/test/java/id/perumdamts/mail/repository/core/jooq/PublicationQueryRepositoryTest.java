package id.perumdamts.mail.repository.core.jooq;

import id.perumdamts.mail.config.SqidsProperties;
import id.perumdamts.mail.dto.core.publication.PublicationParams;
import id.perumdamts.mail.dto.core.publication.PublicationResponse;
import id.perumdamts.mail.entity.master.DocumentType;
import id.perumdamts.mail.enums.PublicationStatus;
import id.perumdamts.mail.util.SqidsEncoder;
import lombok.extern.slf4j.Slf4j;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.impl.DSL.field;

@Slf4j
class PublicationQueryRepositoryTest {

    private static final Field<Long> P_ID = field("p.id", Long.class);
    private static final Field<String> P_TITLE = field("p.title", String.class);
    private static final Field<String> P_DESCRIPTION = field("p.description", String.class);
    private static final Field<Integer> P_TYPE = field("p.type", Integer.class);
    private static final Field<String> JD_JENIS_DOKUMEN = field("jd.jenis_dokumen", String.class);
    private static final Field<String> P_STATUS = field("p.status", String.class);
    private static final Field<LocalDateTime> P_PUBLISHED_DATE = field("p.published_date", LocalDateTime.class);
    private static final Field<String> P_ORIGINAL_FILE_NAME = field("p.original_file_name", String.class);
    private static final Field<String> P_SYSTEM_FILE_NAME = field("p.system_file_name", String.class);
    private static final Field<Integer> P_FILE_SIZE = field("p.file_size", Integer.class);
    private static final Field<String> P_CREATED_BY_NAME = field("p.created_by_name", String.class);
    private static final Field<String> P_CREATED_BY_TITLE = field("p.created_by_title", String.class);
    private static final Field<Integer> P_CREATED_BY_USER_ID = field("p.created_by_user_id", Integer.class);
    private static final Field<LocalDateTime> P_CREATED_AT = field("p.created_at", LocalDateTime.class);
    private static final Field<LocalDateTime> P_UPDATED_AT = field("p.updated_at", LocalDateTime.class);
    private static final Field<Long> TOTAL_COUNT = field("total_count", Long.class);

    @Test
    void findAll_shouldExcludeDeletedAndMapDto() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        PublicationParams params = new PublicationParams();
        params.setSortBy("id");
        params.setSortDir("asc");

        PublicationQueryRepository repository = repositoryWithProvider(ctx -> {
            capturedSql.set(ctx.sql());
            return new MockResult[] {
                    new MockResult(2, publicationRows(true, 2L))
            };
        });

        Page<PublicationResponse> page = repository.findAll(params);

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent().getFirst().getId().value()).isPositive();
        assertThat(page.getContent().getFirst().getDocumentType()).isNotNull();
        assertThat(page.getContent().get(1).getDocumentType()).isNull();
        assertThat(normalize(capturedSql.get())).contains("p.status <> 'deleted'");
        log.info("SQL: {}", capturedSql.get());
    }

    @Test
    void findAll_shouldApplyStatusKeywordTypeAndDateFilters() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        PublicationParams params = new PublicationParams();
        params.setStatus(PublicationStatus.PUBLISHED);
        params.setKeyword("policy");

        // Use a valid Sqid for DocumentType
        SqidsEncoder testEncoder = new SqidsEncoder(new SqidsProperties(null, 0, null, "test-shuffle-key"));
        String typeIdSqid = testEncoder.encode(DocumentType.class, 7L);
        params.setTypeId(typeIdSqid);

        params.setStartDate(LocalDate.of(2026, 1, 1));
        params.setEndDate(LocalDate.of(2026, 1, 31));
        params.setSortBy("title");
        params.setSortDir("desc");
        params.setPage(1);
        params.setSize(5);

        PublicationQueryRepository repository = repositoryWithProvider(ctx -> {
            capturedSql.set(ctx.sql());
            return new MockResult[] {
                    new MockResult(1, publicationRows(true, 1L))
            };
        });

        repository.findAll(params);

        String sql = normalize(capturedSql.get());
        assertThat(sql).contains("p.status = 'published'");
        assertThat(sql).contains("p.type = cast(? as bigint)");
        assertThat(sql).contains("p.published_date between cast(? as date) and cast(? as date)");
        assertThat(sql).contains("order by p.title desc");
        assertThat(sql).contains("fetch next ? rows only");
        assertThat(sql).contains("offset ? rows");

        log.info("SQL with filters: {}", capturedSql.get());
    }

    @Test
    void findById_shouldReturnMappedPublicationWithoutTotalCount() {
        PublicationQueryRepository repository = repositoryWithProvider(_ -> new MockResult[] {
                new MockResult(1, publicationRows(false, null))
        });

        Optional<PublicationResponse> result = repository.findById(10L);

        assertThat(result).isPresent();
        assertThat(result.get().getId().value()).isPositive();
        assertThat(result.get().getDocumentType().getId()).isNotNull();
    }

    @Test
    void findById_shouldReturnEmptyWhenNoRecord() {
        PublicationQueryRepository repository = repositoryWithProvider(_ -> new MockResult[] {
                new MockResult(0, DSL.using(SQLDialect.H2).newResult(P_ID))
        });

        Optional<PublicationResponse> result = repository.findById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void findFileMeta_shouldReturnFileMetaWhenExists() {
        Field<String> P_SYSTEM_FILE_NAME = field("p.system_file_name", String.class);
        PublicationQueryRepository repository = repositoryWithProvider(_ -> {
            DSLContext dsl = DSL.using(SQLDialect.H2);
            Result<Record> result = dsl.newResult(new Field<?>[] { P_ORIGINAL_FILE_NAME, P_SYSTEM_FILE_NAME, P_CREATED_AT });
            Record record = dsl.newRecord(result.fields());
            record.set(P_ORIGINAL_FILE_NAME, "orig.pdf");
            record.set(P_SYSTEM_FILE_NAME, "syst.pdf");
            record.set(P_CREATED_AT, LocalDateTime.of(2026, 1, 1, 10, 0));
            result.add(record);
            return new MockResult[] { new MockResult(1, result) };
        });

        var result = repository.findFileMeta(10L);

        assertThat(result).isPresent();
        assertThat(result.get().originalFileName()).isEqualTo("orig.pdf");
        assertThat(result.get().systemFileName()).isEqualTo("syst.pdf");
        assertThat(result.get().createdAt()).isNotNull();
    }

    private static PublicationQueryRepository repositoryWithProvider(MockDataProvider provider) {
        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.H2);
        SqidsEncoder encoder = new SqidsEncoder(new SqidsProperties(null, 0, null, "test-shuffle-key"));
        return new PublicationQueryRepository(dsl, encoder);
    }

    private static Result<Record> publicationRows(boolean includeCount, Long countValue) {
        DSLContext dsl = DSL.using(SQLDialect.H2);
        Field<?>[] fields = includeCount
                ? new Field<?>[] {
                        P_ID, P_TITLE, P_DESCRIPTION, P_TYPE, JD_JENIS_DOKUMEN, P_STATUS,
                        P_PUBLISHED_DATE, P_ORIGINAL_FILE_NAME, P_SYSTEM_FILE_NAME, P_FILE_SIZE,
                        P_CREATED_BY_NAME, P_CREATED_BY_TITLE, P_CREATED_BY_USER_ID,
                        P_CREATED_AT, P_UPDATED_AT, TOTAL_COUNT
                }
                : new Field<?>[] {
                        P_ID, P_TITLE, P_DESCRIPTION, P_TYPE, JD_JENIS_DOKUMEN, P_STATUS,
                        P_PUBLISHED_DATE, P_ORIGINAL_FILE_NAME, P_SYSTEM_FILE_NAME, P_FILE_SIZE,
                        P_CREATED_BY_NAME, P_CREATED_BY_TITLE, P_CREATED_BY_USER_ID,
                        P_CREATED_AT, P_UPDATED_AT
                };
        Result<Record> result = dsl.newResult(fields);

        LocalDateTime now = LocalDateTime.of(2026, 1, 10, 8, 30);
        Record first = dsl.newRecord(result.fields());
        first.set(P_ID, 10L);
        first.set(P_TITLE, "Policy Update");
        first.set(P_DESCRIPTION, "Internal policy update");
        first.set(P_TYPE, 2);
        first.set(JD_JENIS_DOKUMEN, "Memo");
        first.set(P_STATUS, "PUBLISHED");
        first.set(P_PUBLISHED_DATE, now);
        first.set(P_ORIGINAL_FILE_NAME, "policy.pdf");
        first.set(P_SYSTEM_FILE_NAME, "system-policy.pdf");
        first.set(P_FILE_SIZE, 128);
        first.set(P_CREATED_BY_NAME, "Admin");
        first.set(P_CREATED_BY_TITLE, "Manager");
        first.set(P_CREATED_BY_USER_ID, 11);
        first.set(P_CREATED_AT, now.minusDays(2));
        first.set(P_UPDATED_AT, now.minusDays(1));
        if (includeCount) {
            first.set(TOTAL_COUNT, countValue);
        }
        result.add(first);

        if (includeCount) {
            Record second = dsl.newRecord(result.fields());
            second.set(P_ID, 11L);
            second.set(P_TITLE, "No Type");
            second.set(P_DESCRIPTION, "Without doc type");
            second.set(P_TYPE, null);
            second.set(JD_JENIS_DOKUMEN, null);
            second.set(P_STATUS, "PUBLISHED");
            second.set(P_PUBLISHED_DATE, now);
            second.set(P_ORIGINAL_FILE_NAME, "no-type.pdf");
            second.set(P_SYSTEM_FILE_NAME, "system-no-type.pdf");
            second.set(P_FILE_SIZE, 64);
            second.set(P_CREATED_BY_NAME, "System");
            second.set(P_CREATED_BY_TITLE, "Automated");
            second.set(P_CREATED_BY_USER_ID, 12);
            second.set(P_CREATED_AT, now.minusDays(2));
            second.set(P_UPDATED_AT, now.minusDays(1));
            second.set(TOTAL_COUNT, countValue);
            result.add(second);
        }

        return result;
    }

    private static String normalize(String sql) {
        return sql
                .replace("\"", "")
                .replace("`", "")
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }
}
