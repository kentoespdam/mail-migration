package id.perumdamts.mail.repository.core.jooq;

import id.perumdamts.mail.entity.core.MailArchiveNotifLog;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;
import org.jooq.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class MailArchiveNotifLogQueryRepositoryTest {
    @Mock
    DSLContext dsl;
    @Mock
    SelectJoinStep<org.jooq.Record> selectStep;
    @Mock
    SelectConditionStep<org.jooq.Record> whereStep;
    @Mock
    org.jooq.Record record;

    MailArchiveNotifLogQueryRepository repo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repo = new MailArchiveNotifLogQueryRepository(dsl);
    }

    @Test
    void findById_shouldReturnEntity() {
        when(dsl.selectFrom(any(Table.class))).thenReturn(selectStep);
        when(selectStep.where(any(Condition.class))).thenReturn(whereStep);
        when(whereStep.fetchOne()).thenReturn(record);
        when(record.get("id", Long.class)).thenReturn(1L);
        when(record.get("mail_archive_id", Long.class)).thenReturn(10L);
        when(record.get("user_id", Long.class)).thenReturn(20L);
        when(record.get("notif_date", LocalDateTime.class)).thenReturn(LocalDateTime.now());

        Optional<MailArchiveNotifLog> result = repo.findById(1L);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals(10L, result.get().getMailArchiveId());
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        when(dsl.selectFrom(any(Table.class))).thenReturn(selectStep);
        when(selectStep.where(any(Condition.class))).thenReturn(whereStep);
        when(whereStep.fetchOne()).thenReturn(null);

        Optional<MailArchiveNotifLog> result = repo.findById(999L);
        assertTrue(result.isEmpty());
    }
}