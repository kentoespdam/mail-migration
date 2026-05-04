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

    @Column(name = "ma_mail_date")
    private LocalDate archiveDate;

    @Column(name = "ma_ref_id")
    private Long mailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_mcat_id")
    private MailCategory category;

    @Column(name = "ma_mcat_type")
    private Integer mcatType;

    @Column(name = "ma_mcat_code", length = 32)
    private String mcatCode;

    @Column(name = "ma_org_code", length = 16)
    private String orgCode;

    @Column(name = "ma_org_id")
    private Integer orgId;

    @Column(name = "ma_ref_no", length = 45)
    private String refNo;

    @Column(name = "ma_sent_to", length = 128)
    private String sentTo;

    @Column(name = "ma_subject", length = 256)
    private String subject;

    @Column(name = "ma_content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "ma_note", length = 512)
    private String note;

    @Column(name = "ma_secret_type", length = 45)
    private String secretType;

    @Column(name = "ma_status", nullable = false)
    private Integer status = ArchiveStatus.DRAFT.getDbValue();

    @Column(name = "ma_archive_date")
    private LocalDateTime createdDate;

    @Column(name = "ma_archive_by_name", length = 64)
    private String createdByName;

    @Column(name = "ma_updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "office_code", length = 32)
    private String officeCode;

    @Embedded
    private ArchiveLocation location;

    @Column(name = "ma_keyword_index_flag", length = 256)
    private String keywordFlag;

    @Column(name = "ma_keyword", columnDefinition = "TEXT")
    private String keyword;

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
        this.updatedDate = LocalDateTime.now();
    }

    public void softDelete() {
        this.status = ArchiveStatus.DELETED.getDbValue();
        this.updatedDate = LocalDateTime.now();
    }
}
