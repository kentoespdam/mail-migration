package id.perumdamts.mail.entity.core;

import id.perumdamts.mail.entity.SqidEntity;
import id.perumdamts.mail.entity.master.DocumentType;
import id.perumdamts.mail.enums.PublicationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "area_publik")
@SQLRestriction("status != 'DELETED'")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Publication implements SqidEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "judul", nullable = false)
    private String title;

    @Column(name = "desk", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type")
    private DocumentType documentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PublicationStatus status = PublicationStatus.DRAFT;

    @Column(name = "published_date")
    private LocalDateTime publishedDate;

    @Column(name = "notif_flag", nullable = false)
    private Integer notifFlag = 0;

    @Column(name = "file_name", length = 256)
    private String originalFileName;

    @Column(name = "file_path", length = 256)
    private String systemFileName;

    @Column(name = "file_size")
    private Integer fileSize;

    @Column(name = "created_by_name", length = 128)
    private String createdByName;

    @Column(name = "created_by_title", length = 128)
    private String createdByTitle;

    @Column(name = "created_by_user_id")
    private Integer createdByUserId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Domain Methods ──

    public boolean isDraft() {
        return this.status == PublicationStatus.DRAFT;
    }

    public boolean isPublished() {
        return this.status == PublicationStatus.PUBLISHED;
    }

    public boolean publish() {
        if (this.status != PublicationStatus.DRAFT) {
            return false;
        }
        this.status = PublicationStatus.PUBLISHED;
        this.publishedDate = LocalDateTime.now();
        this.notifFlag = 1;
        this.updatedAt = LocalDateTime.now();
        return true;
    }

    public void softDelete() {
        this.status = PublicationStatus.DELETED;
        this.updatedAt = LocalDateTime.now();
    }
}
