package id.perumdamts.mail.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "mail_folder")
@SQLRestriction("folder_status = 1")
public class MailFolder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "folder_id")
    private Integer id;

    @Column(name = "parent_folder_id", nullable = false)
    private Integer parentFolderId = 0;

    @Column(name = "owner_id", nullable = false, updatable = false)
    private Integer ownerId = 0;

    @Column(name = "folder_icon_cls", length = 45)
    private String iconClass = "email";

    @Column(name = "folder_name", nullable = false, length = 45)
    private String name;

    @Column(name = "folder_status", nullable = false)
    private Integer status = 1;

    @Column(name = "folder_created_date")
    private LocalDateTime createdDate;

    protected MailFolder() {}

    public MailFolder(Integer ownerId, Integer parentFolderId, String name) {
        this.ownerId = ownerId;
        this.parentFolderId = parentFolderId;
        this.name = name;
        this.status = 1;
        this.createdDate = LocalDateTime.now();
    }

    // ── Domain Methods ──

    public boolean isSystemFolder() {
        return ownerId != null && ownerId == 0;
    }

    public boolean isPersonal() {
        return ownerId != null && ownerId > 0;
    }

    public boolean isOwnedBy(Integer userId) {
        return this.ownerId.equals(userId);
    }

    public void rename(String newName) {
        this.name = newName.trim();
    }

    public void softDelete() {
        this.status = 3;
    }

    // ── Getters ──

    public Integer getId() { return id; }

    public Integer getParentFolderId() { return parentFolderId; }

    public Integer getOwnerId() { return ownerId; }

    public String getIconClass() { return iconClass; }

    public String getName() { return name; }

    public Integer getStatus() { return status; }

    public LocalDateTime getCreatedDate() { return createdDate; }
}
