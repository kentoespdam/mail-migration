package id.perumdamts.mail.entity.core;

import id.perumdamts.mail.enums.AttachmentRefType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * Entity untuk tabel {@code attachments}.
 *
 * <p>Legacy schema: composite PK {@code (id, upload_date)}.
 * Di entity kita pakai {@code id} saja sebagai PK (auto-increment).
 *
 * <p>Polymorphic: {@code ref_type} (1=Mail, 2=Archive) + {@code ref_id}.
 * Status: 1=Available, 2=Deleted (soft-delete).
 */
@Entity
@Table(name = "attachments")
@SQLRestriction("status != 2")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "ref_type", nullable = false)
    private Integer refType;

    @Column(name = "ref_id", nullable = false)
    private Long refId;

    @Column(name = "file_ext", length = 8)
    private String fileExt;

    @Column(name = "file_size")
    private Integer fileSize;

    @Column(name = "original_filename", nullable = false, length = 128)
    private String originalFilename;

    @Column(name = "system_filename", nullable = false, length = 128)
    private String systemFilename;

    @Column(name = "doc_notes", length = 128)
    private String docNotes;

    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate;

    @Column(name = "upload_by_name", length = 64)
    private String uploadByName;

    /**
     * 1=Available, 2=Deleted
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    @Column(name = "rec_flag")
    private Integer recFlag;

    @Column(name = "approve_date")
    private LocalDateTime approveDate;

    @Column(name = "approve_by", length = 64)
    private String approveBy;

    public Attachment(AttachmentRefType refType, Long refId,
                      String originalFilename, String systemFilename,
                      String fileExt, Integer fileSize, String uploadByName) {
        this.refType = refType.getDbValue();
        this.refId = refId;
        this.originalFilename = originalFilename;
        this.systemFilename = systemFilename;
        this.fileExt = fileExt;
        this.fileSize = fileSize;
        this.uploadByName = uploadByName;
        this.uploadDate = LocalDateTime.now();
        this.status = 1;
    }

    public AttachmentRefType getRefType() {
        if (refType == null) return null;
        return AttachmentRefType.fromDbValue(refType);
    }

    public boolean isAvailable() {
        return status != null && status == 1;
    }

    public void markDeleted() {
        this.status = 2;
    }
}
