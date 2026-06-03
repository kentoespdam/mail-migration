package id.perumdamts.mail.repository.core.jooq;

import id.perumdamts.mail.entity.core.UserTask;
import id.perumdamts.mail.enums.SystemFolder;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@Repository
@RequiredArgsConstructor
public class UserTaskQueryRepository {

    private final DSLContext dsl;

    public boolean existsActive(Long userId, Long mailId) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(table("sys_user_task"))
                        .where(field("user_id").eq(userId))
                        .and(field("tm_id").eq(mailId))
                        .and(field("folder_id").ne(SystemFolder.PURGED.getId()))
        );
    }

    public Optional<UserTask> findByUserIdAndMailIdAnyFolder(Long userId, Long mailId) {
        return dsl.select(
                        field("tm_id"),
                        field("user_id"),
                        field("folder_id"),
                        field("read_status"),
                        field("restore_folder_id"),
                        field("status")
                )
                .from(table("sys_user_task"))
                .where(field("user_id").eq(userId))
                .and(field("tm_id").eq(mailId))
                .and(field("folder_id").notIn(SystemFolder.PURGED.getId(), SystemFolder.DELETED.getId()))
                .fetchOptional(record -> {
                    UserTask ut = new UserTask();
                    ut.setMailId(record.get("tm_id", Long.class));
                    ut.setUserId(record.get("user_id", Long.class));
                    ut.setFolderId(record.get("folder_id", Long.class));
                    ut.setReadStatus(record.get("read_status", Integer.class));
                    ut.setRestoreFolderId(record.get("restore_folder_id", Long.class));
                    return ut;
                });
    }
}