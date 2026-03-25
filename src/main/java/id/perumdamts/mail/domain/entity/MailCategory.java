package id.perumdamts.mail.domain.entity;

import id.perumdamts.mail.domain.converter.CategoryStatusConverter;
import id.perumdamts.mail.domain.enums.CategoryStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MailCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mcat_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mail_type_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_mcat_mail_type"))
    private MailType mailType;

    @Column(name = "mcat_code", nullable = false, length = 32)
    private String code;

    @Column(name = "mcat_name", nullable = false, length = 64)
    private String name;

    @Convert(converter = CategoryStatusConverter.class)
    @Column(name = "mcat_status", nullable = false)
    private CategoryStatus status = CategoryStatus.ENABLED;

    @Column(name = "sort")
    private Integer sort = 0;

    @Formula("CONCAT(mcat_code, ' - ', mcat_name)")
    private String codeName;

    public MailCategory(MailType mailType, String code, String name) {
        this.mailType = mailType;
        this.code = code;
        this.name = name;
    }

    public boolean isActive() {
        return status == CategoryStatus.ENABLED;
    }

    public void markDeleted() {
        this.status = CategoryStatus.DELETED;
    }

    public void disable() {
        this.status = CategoryStatus.DISABLED;
    }

    public void enable() {
        this.status = CategoryStatus.ENABLED;
    }
}
