package id.perumdamts.mail.repository.core.jooq;

import id.perumdamts.mail.dto.core.folder.FolderCountDto;
import id.perumdamts.mail.enums.SystemFolder;
import org.jooq.DSLContext;
import org.jooq.Record4;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;

/**
 * Repository untuk menghitung counter badge unread per folder.
 * Menggunakan JOOQ window function — 1 query, bukan N+1.
 */
@Repository
public class FolderCounterRepository {

        private final DSLContext dsl;

        public FolderCounterRepository(DSLContext dsl) {
                this.dsl = dsl;
        }

        /**
         * Hitung unread & total mail per folder untuk user tertentu.
         * Menggunakan window function untuk efisiensi (1 query).
         *
         * @param userId ID user
         * @return Map<folderId, FolderCountDto>
         */
        public Map<Long, FolderCountDto> getCountersMap(Long userId) {
                // Query dengan window function untuk hitung unread dan total per folder
                List<Record4<Long, String, Long, Long>> records = dsl.select(
                                field("fx.folder_id", Long.class),
                                field("fx.folder_name", String.class),
                                sum(when(field("ut.read_status").eq(0), 1).otherwise(0)).cast(Long.class)
                                                .as("unread_count"),
                                count(field("ut.user_task_id")).cast(Long.class).as("total_count"))
                                .from(table("mail_folder").as("fx"))
                                .leftJoin(table("sys_user_task").as("ut"))
                                .on(field("ut.folder_id").eq(field("fx.folder_id"))
                                                .and(field("ut.user_id").eq(userId)))
                                .where(field("fx.owner_id").in(0L, userId))
                                .and(field("fx.folder_id").notIn(
                                        SystemFolder.ROOT.getId(),
                                        SystemFolder.PERSONAL_ROOT.getId()))
                                .and(field("fx.folder_status").eq(1))
                                .groupBy(field("fx.folder_id"), field("fx.folder_name"))
                                .fetch();

                return records.stream()
                                .map(r -> new FolderCountDto(
                                                r.value1(),
                                                r.value2(),
                                                r.value3() != null ? r.value3() : 0L,
                                                r.value4() != null ? r.value4() : 0L))
                                .collect(Collectors.toMap(FolderCountDto::folderId, dto -> dto));
        }

        /**
         * Hitung counter untuk folder tertentu saja.
         */
        public FolderCountDto getCounterForFolder(Long userId, Long folderId) {
                Record4<Long, String, Long, Long> record = dsl.select(
                                field("fx.folder_id", Long.class),
                                field("fx.folder_name", String.class),
                                sum(when(field("ut.read_status").eq(0), 1).otherwise(0)).cast(Long.class)
                                                .as("unread_count"),
                                count(field("ut.user_task_id")).cast(Long.class).as("total_count"))
                                .from(table("mail_folder").as("fx"))
                                .leftJoin(table("sys_user_task").as("ut"))
                                .on(field("ut.folder_id").eq(field("fx.folder_id"))
                                                .and(field("ut.user_id").eq(userId)))
                                .where(field("fx.folder_id").eq(folderId))
                                .and(field("fx.owner_id").in(0L, userId))
                                .and(field("fx.folder_status").eq(1))
                                .groupBy(field("fx.folder_id"), field("fx.folder_name"))
                                .fetchOne();

                if (record == null) {
                        return new FolderCountDto(folderId, null, 0L, 0L);
                }

                return new FolderCountDto(
                                record.value1(),
                                record.value2(),
                                record.value3() != null ? record.value3() : 0L,
                                record.value4() != null ? record.value4() : 0L);
        }
}
