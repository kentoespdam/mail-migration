package id.perumdamts.mail.domain.entity;

import id.perumdamts.mail.domain.enums.RecordStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

/**
 * Entity untuk tabel {@code pesan_singkat}.
 *
 * <p>Legacy schema: hanya {@code id} dan {@code pesan}.
 * Flyway V5 menambahkan {@code status} (varchar) untuk soft-delete.
 */
@Entity
@Table(name = "pesan_singkat")
@SQLRestriction("status != 'DELETED'")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class QuickMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "pesan", nullable = false, length = 128)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private RecordStatus status = RecordStatus.ACTIVE;

    public QuickMessage(String message) {
        this.message = message;
    }

    public boolean isActive() {
        return status == RecordStatus.ACTIVE;
    }

    public void markDeleted() {
        this.status = RecordStatus.DELETED;
    }
}
