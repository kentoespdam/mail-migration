package id.perumdamts.mail.domain.entity;

import id.perumdamts.mail.domain.enums.ReadStatus;
import id.perumdamts.mail.domain.enums.SystemFolder;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sys_user_task", indexes = {
        @Index(name = "idx_ut_user_folder", columnList = "user_id, folder_id"),
        @Index(name = "idx_ut_user_mail", columnList = "user_id, tm_id")
})
public class UserTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_task_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "tm_id", nullable = false)
    private Integer mailId;

    @Column(name = "folder_id", nullable = false)
    private Integer folderId;

    @Column(name = "read_status", nullable = false)
    private Integer readStatus = ReadStatus.UNREAD.getDbValue();

    @Column(name = "read_date")
    private LocalDateTime readDate;

    @Column(name = "restore_folder_id")
    private Integer restoreFolderId;

    @Column(name = "mail_created_date")
    private LocalDateTime mailCreatedDate;

    protected UserTask() {}

    public UserTask(Integer userId, Integer mailId, int folderId) {
        this.userId = userId;
        this.mailId = mailId;
        this.folderId = folderId;
        this.readStatus = ReadStatus.UNREAD.getDbValue();
        this.mailCreatedDate = LocalDateTime.now();
    }

    public static UserTask inbox(Integer userId, Integer mailId) {
        return new UserTask(userId, mailId, SystemFolder.INBOX.getId());
    }

    public static UserTask draft(Integer userId, Integer mailId) {
        var task = new UserTask(userId, mailId, SystemFolder.DRAFT.getId());
        task.readStatus = ReadStatus.READ.getDbValue();
        return task;
    }

    // ── Domain Methods ──

    public void markRead() {
        this.readStatus = ReadStatus.READ.getDbValue();
        this.readDate = LocalDateTime.now();
    }

    public void moveToFolder(int targetFolderId) {
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
        return this.folderId == SystemFolder.DELETED.getId();
    }

    public boolean isPurged() {
        return this.folderId == SystemFolder.PURGED.getId();
    }

    // ── Getters & Setters ──

    public Long getId() { return id; }

    public Integer getUserId() { return userId; }

    public Integer getMailId() { return mailId; }

    public Integer getFolderId() { return folderId; }
    public void setFolderId(Integer folderId) { this.folderId = folderId; }

    public Integer getReadStatus() { return readStatus; }

    public LocalDateTime getReadDate() { return readDate; }

    public Integer getRestoreFolderId() { return restoreFolderId; }

    public LocalDateTime getMailCreatedDate() { return mailCreatedDate; }
    public void setMailCreatedDate(LocalDateTime mailCreatedDate) { this.mailCreatedDate = mailCreatedDate; }
}
