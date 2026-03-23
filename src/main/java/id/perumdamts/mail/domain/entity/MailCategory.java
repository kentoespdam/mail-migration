package id.perumdamts.mail.domain.entity;

import id.perumdamts.mail.domain.enums.CategoryStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.SQLRestriction;

/**
 * Entity untuk tabel {@code mail_category}.
 *
 * <p>Unique constraint pada (mail_type_id, mcat_code) —
 * tidak boleh ada kode kategori duplikat dalam satu tipe surat.
 *
 * <p>{@code codeName} adalah formula gabungan kode + nama untuk display.
 */
@Entity
@Table(name = "mail_category", uniqueConstraints = {
        @UniqueConstraint(name = "uq_category_type_code", columnNames = {"mail_type_id", "mcat_code"})
})
@SQLRestriction("mcat_status != 'Deleted'")
public class MailCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mcat_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mail_type_id", nullable = false)
    private MailType mailType;

    @Column(name = "mcat_code", nullable = false, length = 32)
    private String code;

    @Column(name = "mcat_name", nullable = false, length = 64)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "mcat_status", nullable = false)
    private CategoryStatus status = CategoryStatus.ENABLED;

    @Column(name = "sort")
    private Integer sort = 0;

    @Formula("CONCAT(mcat_code, ' - ', mcat_name)")
    private String codeName;

    protected MailCategory() {}

    public MailCategory(MailType mailType, String code, String name) {
        this.mailType = mailType;
        this.code = code;
        this.name = name;
        this.status = CategoryStatus.ENABLED;
    }

    // ── Getters & Setters ──

    public Integer getId() { return id; }

    public MailType getMailType() { return mailType; }
    public void setMailType(MailType mailType) { this.mailType = mailType; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public CategoryStatus getStatus() { return status; }

    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }

    public String getCodeName() { return codeName; }

    public boolean isActive() { return status == CategoryStatus.ENABLED; }

    public void markDeleted() { this.status = CategoryStatus.DELETED; }

    public void disable() { this.status = CategoryStatus.DISABLED; }

    public void enable() { this.status = CategoryStatus.ENABLED; }
}
