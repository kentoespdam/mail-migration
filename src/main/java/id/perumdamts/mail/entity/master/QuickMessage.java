package id.perumdamts.mail.entity.master;

import id.perumdamts.mail.entity.SqidEntity;
import id.perumdamts.mail.enums.RecordStatusActive;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * Entity untuk tabel {@code pesan_singkat}.
 *
 * <p>
 * Legacy schema: hanya {@code id} dan {@code pesan}.
 * Flyway V5 menambahkan {@code status} (varchar) untuk soft-delete.
 */
@Entity
@Table(name = "pesan_singkat")
@SQLRestriction("is_deleted = 0")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class QuickMessage implements SqidEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "pesan", nullable = false, length = 128)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_new", nullable = false)
    private RecordStatusActive status = RecordStatusActive.ACTIVE;

    @Column(name = "is_deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    public QuickMessage(String message) {
        this.message = message;
    }

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = this.createdDate;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = LocalDateTime.now();
    }

    public boolean isActive() {
        return !this.deleted && this.status == RecordStatusActive.ACTIVE;
    }

    public void markDeleted() {
        this.deleted = true;
    }

    public void toggleStatus() {
        if (this.deleted) {
            throw new IllegalStateException("Cannot toggle status of a deleted record");
        }
        this.status = (status == RecordStatusActive.ACTIVE) ? RecordStatusActive.INACTIVE : RecordStatusActive.ACTIVE;
    }
}
