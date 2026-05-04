package id.perumdamts.mail.entity.core;

import id.perumdamts.mail.entity.SqidEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * Mail folder yang dibuat oleh user.
 * DB column {@code folder_status} adalah INT: 1=Active, 3=Deleted (soft).
 */
@Entity
@Table(name = "mail_folder")
@SQLRestriction("folder_status = 1")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MailFolder implements SqidEntity {

    private static final int STATUS_ACTIVE = 1;
    private static final int STATUS_DELETED = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "folder_id")
    private Long id;

    @Column(name = "parent_folder_id")
    private Long parentFolderId;

    @Column(name = "owner_id", nullable = false, updatable = false)
    private Long ownerId;

    @Column(name = "folder_icon_cls", length = 45)
    private String iconClsFolder;

    @Column(name = "folder_name", nullable = false, length = 45)
    private String name;

    @Column(name = "folder_status", nullable = false)
    private Integer status = STATUS_ACTIVE;

    @Column(name = "folder_created_date")
    private LocalDateTime createdDate;

    public MailFolder(Long ownerId, Long parentFolderId, String name) {
        this.ownerId = ownerId;
        this.parentFolderId = (parentFolderId != null && parentFolderId > 0) ? parentFolderId : null;
        this.name = name.trim();
        this.iconClsFolder = "folder";
        this.status = STATUS_ACTIVE;
        this.createdDate = LocalDateTime.now();
    }

    // ── Domain Methods ──

    public boolean isSystemFolder() {
        return this.ownerId != null && this.ownerId == 0;
    }

    public boolean isOwnedBy(Long userId) {
        return this.ownerId.equals(userId);
    }

    public void rename(String newName) {
        this.name = newName.trim();
    }

    /** Soft delete — set folder_status=3 (DELETED) */
    public void softDelete() {
        this.status = STATUS_DELETED;
    }

    public boolean isDeleted() {
        return this.status == STATUS_DELETED;
    }
}
