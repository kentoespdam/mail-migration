package id.perumdamts.mail.entity.core;

import id.perumdamts.mail.enums.ReadStatus;
import id.perumdamts.mail.enums.SystemFolder;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "sys_user_task", indexes = {
        @Index(name = "idx_ut_user_folder", columnList = "user_id, folder_id"),
        @Index(name = "idx_ut_user_mail", columnList = "user_id, tm_id")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_task_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "tm_id", nullable = false)
    private Long mailId;

    @Column(name = "folder_id", nullable = false)
    private Long folderId;

    @Column(name = "read_status", nullable = false)
    private Integer readStatus = ReadStatus.UNREAD.getDbValue();

    @Column(name = "read_date")
    private LocalDateTime readDate;

    @Column(name = "restore_folder_id")
    private Long restoreFolderId;

    @Column(name = "mail_created_date")
    private LocalDateTime mailCreatedDate;

    public UserTask(Long userId, Long mailId, Long folderId) {
        this.userId = userId;
        this.mailId = mailId;
        this.folderId = folderId;
        this.readStatus = ReadStatus.UNREAD.getDbValue();
        this.mailCreatedDate = LocalDateTime.now();
    }

    public static UserTask inbox(Long userId, Long mailId) {
        return new UserTask(userId, mailId, SystemFolder.INBOX.getId());
    }

    public static UserTask draft(Long userId, Long mailId) {
        var task = new UserTask(userId, mailId, SystemFolder.DRAFT.getId());
        task.readStatus = ReadStatus.READ.getDbValue();
        return task;
    }

    // ── Domain Methods ──

    public void markRead() {
        this.readStatus = ReadStatus.READ.getDbValue();
        this.readDate = LocalDateTime.now();
    }

    public void moveToFolder(Long targetFolderId) {
        this.folderId = targetFolderId;
    }

    public void softDelete() {
        this.restoreFolderId = this.folderId;
        this.folderId = SystemFolder.DELETED.getId();
    }

    public void restore() {
        if (this.restoreFolderId == null) {
            throw new IllegalStateException("No restore folder recorded");
        }
        this.folderId = this.restoreFolderId;
        this.restoreFolderId = null;
    }

    public void purge() {
        this.folderId = SystemFolder.PURGED.getId();
    }

    public boolean isInTrash() {
        return this.folderId.equals(SystemFolder.DELETED.getId());
    }

    public boolean isPurged() {
        return Objects.equals(this.folderId, SystemFolder.PURGED.getId());
    }
}
