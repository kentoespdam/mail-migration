package id.perumdamts.mail.service.core.usertask;

import id.perumdamts.mail.dto.core.mail.MailLookupParams;
import id.perumdamts.mail.dto.core.mail.MailLookupResponse;
import id.perumdamts.mail.dto.core.mail.MailTrackingItemResponse;
import id.perumdamts.mail.entity.core.Mail;
import id.perumdamts.mail.entity.core.UserTask;
import id.perumdamts.mail.enums.ReadStatus;
import id.perumdamts.mail.enums.SystemFolder;
import id.perumdamts.mail.repository.core.jpa.UserTaskRepository;
import id.perumdamts.mail.util.SqidsEncoder;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.jooq.impl.DSL.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserTaskQueryService {
    private final DSLContext dsl;
    private final UserTaskRepository userTaskRepository;
    private final SqidsEncoder encoder;

    public boolean existsActive(Long userId, Long mailId) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(table("sys_user_task"))
                        .where(field("user_id").eq(userId))
                        .and(field("tm_id").eq(mailId))
                        .and(field("folder_id").ne(SystemFolder.PURGED.getId()))
        );
    }

    public long countUnread(Long userId, Long folderId) {
        return dsl.selectCount()
                .from(table("sys_user_task"))
                .where(field("user_id").eq(userId))
                .and(field("folder_id").eq(folderId))
                .and(field("read_status").eq(ReadStatus.UNREAD.getDbValue()))
                .fetchOne(0, Long.class);
    }

    public Optional<UserTask> findUserTask(Long userId, Long mailId) {
        return userTaskRepository.findByUserIdAndMailIdAnyFolder(userId, mailId);
    }

    public Long resolveRootId(Long mailId) {
        Long rootId = dsl.select(field("m_root_id", Long.class))
                .from(table("mail"))
                .where(field("m_id").eq(mailId))
                .fetchOneInto(Long.class);
        return rootId != null ? rootId : mailId;
    }

    public Page<MailLookupResponse> findAll(Long userId, MailLookupParams params) {
        Condition condition = field("ut.user_id").eq(userId)
                .and(field("m.m_status").eq(1)); // SENT only

        if (params.getFolderId() != null && !params.getFolderId().isBlank()) {
            Long folderId = encoder.decode(id.perumdamts.mail.entity.core.MailFolder.class, params.getFolderId());
            condition = condition.and(field("ut.folder_id").eq(folderId));
        } else {
            condition = condition.and(field("ut.folder_id").notIn(
                    SystemFolder.DELETED.getId(),
                    SystemFolder.PURGED.getId()
            ));
        }

        var records = dsl.select(
                        field("m.m_id").as("id"),
                        field("m.m_date").as("mailDate"),
                        field("m.m_created_by_name").as("createdByName"),
                        field("m.m_subject").as("subject"),
                        field("mt.mail_type").as("typeName"),
                        field("mc.mcat_name").as("categoryName"),
                        field("m.m_max_response_date").as("maxResponseDate"),
                        field("ut.read_status").as("readStatus"),
                        case_(field("r.circulation"))
                                .when(inline(1), inline("DISPOSISI"))
                                .when(inline(2), inline("MEMO_MANDIRI"))
                                .when(inline(3), inline("MEMO"))
                                .when(inline(4), inline("CC"))
                                .when(inline(5), inline("REPLY"))
                                .when(inline(6), inline("FORWARD"))
                                .otherwise(case_()
                                        .when(field("m.m_created_by").eq(userId), inline("SENDER"))
                                        .otherwise(inline("UNKNOWN")))
                                .as("circulationName"),
                        count().over().as("total_count"))
                .from(table("sys_user_task").as("ut"))
                .join(table("mail").as("m")).on(field("m.m_id").eq(field("ut.tm_id")))
                .leftJoin(table("mail_type").as("mt")).on(field("mt.mail_type_id").eq(field("m.m_type")))
                .leftJoin(table("mail_category").as("mc")).on(field("mc.mcat_id").eq(field("m.m_category")))
                .leftJoin(table("mail_recipient").as("r")).on(field("r.mail_id").eq(field("m.m_id")).and(field("r.user_id").eq(userId)))
                .where(condition)
                .orderBy(params.toSortField())
                .limit(params.getSize())
                .offset(params.offset())
                .fetch();

        long total = records.isEmpty() ? 0 : records.getFirst().get(field("total_count"), Long.class);
        List<MailLookupResponse> content = records.map(this::mapToLookupResponse);

        return new PageImpl<>(content, PageRequest.of(params.getPage(), params.getSize()), total);
    }

    public List<MailTrackingItemResponse> findThread(Long rootMailId) {
        return dsl.select(
                        field("m.m_id").as("id"),
                        field("m.m_no").as("mailNumber"),
                        field("m.m_date").as("mailDate"),
                        field("m.m_subject").as("subject"),
                        field("m.m_created_by_name").as("createdByName"),
                        field("m.m_created_date").as("createdDate"))
                .from(table("mail").as("m"))
                .where(field("m.m_root_id").eq(rootMailId))
                .and(field("m.m_status").eq(1)) // SENT only
                .orderBy(field("m.m_created_date").asc())
                .fetch(this::mapToTrackingItemResponse);
    }

    private MailLookupResponse mapToLookupResponse(Record r) {
        return new MailLookupResponse(
                encoder.encode(Mail.class, r.get("id", Long.class)),
                r.get("mailDate", LocalDate.class),
                r.get("createdByName", String.class),
                r.get("subject", String.class),
                r.get("typeName", String.class),
                r.get("categoryName", String.class),
                r.get("circulationName", String.class),
                r.get("maxResponseDate", LocalDate.class),
                ReadStatus.READ.getDbValue() == r.get("readStatus", Integer.class)
        );
    }

    private MailTrackingItemResponse mapToTrackingItemResponse(Record r) {
        return new MailTrackingItemResponse(
                encoder.encode(Mail.class, r.get("id", Long.class)),
                r.get("mailNumber", String.class),
                r.get("mailDate", LocalDate.class),
                r.get("subject", String.class),
                r.get("createdByName", String.class),
                r.get("createdDate", LocalDateTime.class)
        );
    }
}
