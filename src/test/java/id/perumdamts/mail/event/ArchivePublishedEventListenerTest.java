package id.perumdamts.mail.event;

import id.perumdamts.mail.integration.hr.EmployeeDto;
import id.perumdamts.mail.integration.hr.HrServiceClient;
import id.perumdamts.mail.integration.hr.PageResponse;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArchivePublishedEventListenerTest {

    @Mock
    private HrServiceClient hrServiceClient;

    private ArchivePublishedEventListener listener;

    private AtomicInteger insertLogCount;
    private AtomicInteger upsertNotifCount;
    private StringBuilder capturedSql;

    @BeforeEach
    void setUp() {
        insertLogCount = new AtomicInteger(0);
        upsertNotifCount = new AtomicInteger(0);
        capturedSql = new StringBuilder();

        MockDataProvider provider = ctx -> {
            String sql = ctx.sql().toLowerCase();
            capturedSql.append(sql).append("\n");
            if (sql.contains("insert into mail_archive_notif_log")) {
                insertLogCount.incrementAndGet();
            } else if (sql.contains("insert into mail_archive_notif")) {
                upsertNotifCount.incrementAndGet();
            }

            DSLContext dsl = DSL.using(SQLDialect.MYSQL);
            if (sql.contains("select 1") || sql.contains("select exists")) {
                // For fetchExists/selectOne, return a row with 0/1 or false/true
                Field<Integer> field = DSL.field("exists_val", Integer.class);
                Result<Record1<Integer>> result = dsl.newResult(field);
                result.add(dsl.newRecord(field).values(0));
                return new MockResult[]{new MockResult(1, result)};
            }

            return new MockResult[]{new MockResult(1, dsl.newResult())};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.MYSQL);
        listener = new ArchivePublishedEventListener(dsl, hrServiceClient);
    }

    @Test
    void onArchivePublished_shouldResolvePositionsAndInsertLogsAndNotif() {
        Long archiveId = 1L;
        List<Integer> positionIds = List.of(101, 102);
        ArchivePublishedEvent event = new ArchivePublishedEvent(archiveId, 1, "Publisher", "OFFICE", positionIds);

        // Mock HR Service responses
        EmployeeDto emp1 = new EmployeeDto(1001L, "N1", "E1", "AKTIF", null, null, null);
        EmployeeDto emp2 = new EmployeeDto(1002L, "N2", "E2", "AKTIF", null, null, null);
        EmployeeDto emp3 = new EmployeeDto(1003L, "N3", "E3", "AKTIF", null, null, null);

        when(hrServiceClient.searchEmployees(isNull(), isNull(), eq(101L), isNull(), isNull(), anyInt(), anyInt()))
                .thenReturn(new PageResponse<>(List.of(emp1, emp2), 1, 2, 0, 20));
        when(hrServiceClient.searchEmployees(isNull(), isNull(), eq(102L), isNull(), isNull(), anyInt(), anyInt()))
                .thenReturn(new PageResponse<>(List.of(emp3), 1, 1, 0, 20));

        listener.onArchivePublished(event);

        // Verify SQL calls
        // Should have inserted 3 log records (emp1, emp2, emp3)
        // Note: if implemented as batch, it might be 1 call with multiple rows
        // If implemented as individual inserts, it might be 3 calls.
        // Let's check the SQL content instead of exact count if we use batch.
        String sql = capturedSql.toString();
        assertThat(sql).contains("mail_archive_notif_log");
        assertThat(sql).contains("mail_archive_notif");
        
        // Check for specific columns as per legacy schema mentioned in issue
        assertThat(sql).contains("notif_flag");
        assertThat(sql).contains("processed_date");
        assertThat(sql).contains("updated_at");
        
        // user_id should be in mail_archive_notif_log but NOT in mail_archive_notif
        assertThat(sql).contains("insert into mail_archive_notif_log");
        String notifInsert = java.util.Arrays.stream(sql.split("\n"))
                .filter(line -> line.contains("insert into mail_archive_notif "))
                .findFirst().orElse("");
        assertThat(notifInsert).doesNotContain("user_id");
    }
}
