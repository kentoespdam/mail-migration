package id.perumdamts.mail.repository.core.jpa;

import id.perumdamts.mail.entity.core.UserTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserTaskRepository extends JpaRepository<UserTask, Long> {

    Optional<UserTask> findByUserIdAndMailId(Integer userId, Integer mailId);

    @Query("SELECT u FROM UserTask u WHERE u.userId = :userId AND u.mailId = :mailId AND u.folderId != -1")
    Optional<UserTask> findActiveByUserIdAndMailId(@Param("userId") Integer userId,
                                                    @Param("mailId") Integer mailId);

    @Query("SELECT u FROM UserTask u WHERE u.userId = :userId AND u.mailId = :mailId")
    Optional<UserTask> findByUserIdAndMailIdAnyFolder(@Param("userId") Integer userId,
                                                       @Param("mailId") Integer mailId);

    long countByUserIdAndFolderIdAndReadStatus(Integer userId, Integer folderId, Integer readStatus);

    @Modifying
    @Query("UPDATE UserTask u SET u.folderId = :toFolder WHERE u.userId = :userId AND u.mailId = :mailId AND u.folderId = :fromFolder")
    int updateFolder(@Param("userId") Integer userId,
                     @Param("mailId") Integer mailId,
                     @Param("fromFolder") int fromFolder,
                     @Param("toFolder") int toFolder);

    @Modifying
    @Query("UPDATE UserTask u SET u.folderId = -1, u.restoreFolderId = 6 WHERE u.userId = :userId AND u.folderId = 6")
    int emptyTrash(@Param("userId") Integer userId);

    @Modifying
    @Query("UPDATE UserTask u SET u.folderId = :targetFolderId WHERE u.userId = :userId AND u.folderId = :sourceFolderId")
    int relocateMails(@Param("userId") Integer userId,
                      @Param("sourceFolderId") Integer sourceFolderId,
                      @Param("targetFolderId") Integer targetFolderId);

    /**
     * Purge semua mail di folder DELETED (folder_id=6) untuk user tertentu.
     * Set folder_id = -1 (PURGED) dan clear restore_folder_id.
     */
    @Modifying
    @Query("UPDATE UserTask u SET u.folderId = -1, u.restoreFolderId = null WHERE u.userId = :userId AND u.folderId = 6")
    int purgeTrash(@Param("userId") Integer userId);

    /**
     * Restore mail dari trash ke folder asal.
     * restore_folder_id diambil dari DB, bukan dari client.
     */
    @Modifying
    @Query("UPDATE UserTask u SET u.folderId = u.restoreFolderId, u.restoreFolderId = null WHERE u.userId = :userId AND u.mailId = :mailId AND u.folderId = 6")
    int restoreFromTrash(@Param("userId") Integer userId,
                         @Param("mailId") Integer mailId);

    /**
     * Get semua UserTask untuk mail tertentu di folder DELETED.
     */
    @Query("SELECT u FROM UserTask u WHERE u.mailId = :mailId AND u.folderId = 6")
    List<UserTask> findAllInTrashByMailId(@Param("mailId") Integer mailId);
}
