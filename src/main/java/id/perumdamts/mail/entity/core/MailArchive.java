package id.perumdamts.mail.entity.core;

import id.perumdamts.mail.entity.master.MailCategory;
import id.perumdamts.mail.enums.ArchiveStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "mail_archive")
@SQLRestriction("ma_status != 3")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MailArchive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_id")
    private Long id;

    @Column(name = "ma_no", length = 64)
    private String archiveNumber;

    @Column(name = "ma_date")
    private LocalDate archiveDate;

    @Column(name = "ma_mail_id")
    private Long mailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_category")
    private MailCategory category;

    @Column(name = "ma_subject", length = 256)
    private String subject;

    @Column(name = "ma_content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "ma_status", nullable = false)
    private Integer status = ArchiveStatus.DRAFT.getDbValue();

    @Column(name = "ma_created_date")
    private LocalDateTime createdDate;

    @Column(name = "ma_updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "ma_created_by")
    private Integer createdBy;

    @Column(name = "ma_created_by_name", length = 64)
    private String createdByName;

    @Column(name = "ma_attachment_qty")
    private Integer attachmentQty = 0;

    @Column(name = "ma_year")
    private Short year;

    @Column(name = "ma_office_code", length = 32)
    private String officeCode;

    @Embedded
    private ArchiveLocation location;

    @Column(name = "ma_keyword_flag", length = 256)
    private String keywordFlag;

    @Column(name = "ma_published_at")
    private LocalDateTime publishedAt;

    // ── Domain Methods ──

    public ArchiveStatus getArchiveStatus() {
        return ArchiveStatus.fromDbValue(this.status);
    }

    public boolean isDraft() {
        return this.status == ArchiveStatus.DRAFT.getDbValue();
    }

    public boolean isArchived() {
        return this.status == ArchiveStatus.ARCHIVED.getDbValue();
    }

    public void publish(String archiveNumber) {
        this.archiveNumber = archiveNumber;
        this.status = ArchiveStatus.ARCHIVED.getDbValue();
        this.publishedAt = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    public void softDelete() {
        this.status = ArchiveStatus.DELETED.getDbValue();
        this.updatedDate = LocalDateTime.now();
    }
}
