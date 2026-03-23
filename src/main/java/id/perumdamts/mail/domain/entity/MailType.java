package id.perumdamts.mail.domain.entity;

import id.perumdamts.mail.domain.enums.RecordStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

/**
 * Entity untuk tabel {@code mail_type}.
 *
 * <p>Legacy schema: status 1=Active, 3=Deleted (int).
 * Di entity baru kita map ke {@link RecordStatus} enum string
 * karena Flyway V5 sudah mengubah kolom ke varchar/enum.
 *
 * <p>Composite PK legacy {@code (mail_type_id, mail_type)} diabaikan —
 * kita pakai {@code mail_type_id} saja sebagai PK karena auto-increment.
 */
@Entity
@Table(name = "mail_type")
@SQLRestriction("mail_type_status != 3")
@NoArgsConstructor
@Getter
@Setter
public class MailType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mail_type_id")
    private Integer id;

    @Column(name = "mail_type", nullable = false, length = 32)
    private String name;

    /**
     * Legacy: 1 = Active, 3 = Deleted.
     */
    @Column(name = "mail_type_status", nullable = false)
    private Integer status = 1;

    public MailType(String name) {
        this.name = name;
        this.status = 1;
    }

    public MailType(Integer id, String name, Integer status) {
        this.id = id;
        this.name = name;
        this.status = status;
    }

    public boolean isActive() {
        return status != null && status == 1;
    }

    public void markDeleted() {
        this.status = 3;
    }
}
