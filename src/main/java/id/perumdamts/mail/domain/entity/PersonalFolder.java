package id.perumdamts.mail.domain.entity;

import id.perumdamts.mail.domain.enums.RecordStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * Personal folder yang dibuat oleh user.
 * Menggunakan soft delete 2-level: Trash (folder DELETED=6) → Purge (hapus permanent).
 */
@Entity
@Table(name = "mail_folder")
@SQLRestriction("folder_status != 3")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PersonalFolder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "folder_id")
    private Integer id;

    @Column(name = "parent_folder_id", nullable = false)
    private Integer parentFolderId;

    @Column(name = "owner_id", nullable = false, updatable = false)
    private Integer ownerId;

    @Column(name = "folder_icon_cls", length = 45)
    private String iconClsFolder;

    @Column(name = "folder_name", nullable = false, length = 45)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "folder_status", nullable = false)
    private RecordStatus status = RecordStatus.ACTIVE;

    @Column(name = "folder_created_date")
    private LocalDateTime createdDate;

    public PersonalFolder(Integer ownerId, Integer parentFolderId, String name) {
        this.ownerId = ownerId;
        this.parentFolderId = parentFolderId;
        this.name = name.trim();
        this.iconClsFolder = "folder";
        this.createdDate = LocalDateTime.now();
    }

    // ── Domain Methods ──

    /**
     * Apakah folder ini adalah folder sistem (tidak bisa dimodifikasi user)?
     */
    public boolean isSystemFolder() {
        return this.ownerId != null && this.ownerId == 0;
    }

    /**
     * Apakah folder ini dimiliki oleh user tertentu?
     */
    public boolean isOwnedBy(Integer userId) {
        return this.ownerId.equals(userId);
    }

    /**
     * Rename folder
     */
    public void rename(String newName) {
        this.name = newName.trim();
    }

    /**
     * Soft delete — pindah ke status INACTIVE (folder_status=3)
     */
    public void softDelete() {
        this.status = RecordStatus.INACTIVE;
    }

    /**
     * Apakah folder ini sudah di-trash (soft deleted)?
     */
    public boolean isDeleted() {
        return this.status == RecordStatus.INACTIVE;
    }
}
