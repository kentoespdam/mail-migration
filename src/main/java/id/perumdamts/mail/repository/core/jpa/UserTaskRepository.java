package id.perumdamts.mail.repository.core.jpa;

import id.perumdamts.mail.entity.core.UserTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserTaskRepository extends JpaRepository<UserTask, Long> {

    Optional<UserTask> findByUserIdAndMailId(Long userId, Long mailId);

    @Query("SELECT u FROM UserTask u WHERE u.userId = :userId AND u.mailId = :mailId AND u.folderId != -1")
    Optional<UserTask> findActiveByUserIdAndMailId(@Param("userId") Long userId,
            @Param("mailId") Long mailId);

    @Query("SELECT u FROM UserTask u WHERE u.userId = :userId AND u.mailId = :mailId")
    Optional<UserTask> findByUserIdAndMailIdAnyFolder(@Param("userId") Long userId,
            @Param("mailId") Long mailId);

    long countByUserIdAndFolderIdAndReadStatus(Long userId, Long folderId, Integer readStatus);

    @Modifying
    @Query("UPDATE UserTask u SET u.folderId = :toFolder WHERE u.userId = :userId AND u.mailId = :mailId AND u.folderId = :fromFolder")
    void updateFolder(@Param("userId") Long userId,
            @Param("mailId") Long mailId,
            @Param("fromFolder") Long fromFolder,
            @Param("toFolder") Long toFolder);

    @Modifying
    @Query("UPDATE UserTask u SET u.folderId = -1, u.restoreFolderId = 6 WHERE u.userId = :userId AND u.folderId = 6")
    int emptyTrash(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE UserTask u SET u.folderId = :targetFolderId WHERE u.userId = :userId AND u.folderId = :sourceFolderId")
    void relocateMails(@Param("userId") Long userId,
            @Param("sourceFolderId") Long sourceFolderId,
            @Param("targetFolderId") Long targetFolderId);

    /**
     * Purge semua mail di folder DELETED (folder_id=6) untuk user tertentu.
     * Set folder_id = -1 (PURGED) dan clear restore_folder_id.
     */
    @Modifying
    @Query("UPDATE UserTask u SET u.folderId = -1, u.restoreFolderId = null WHERE u.userId = :userId AND u.folderId = 6")
    void purgeTrash(@Param("userId") Long userId);

    /**
     * Restore mail dari trash ke folder asal.
     * restore_folder_id diambil dari DB, bukan dari client.
     */
    @Modifying
    @Query("UPDATE UserTask u SET u.folderId = u.restoreFolderId, u.restoreFolderId = null WHERE u.userId = :userId AND u.mailId = :mailId AND u.folderId = 6")
    int restoreFromTrash(@Param("userId") Long userId,
            @Param("mailId") Long mailId);

    /**
     * Get semua UserTask untuk mail tertentu di folder DELETED.
     */
    @Query("SELECT u FROM UserTask u WHERE u.mailId = :mailId AND u.folderId = 6")
    List<UserTask> findAllInTrashByMailId(@Param("mailId") Long mailId);

    long countByUserIdAndFolderId(Long userId, Long folderId);
}
