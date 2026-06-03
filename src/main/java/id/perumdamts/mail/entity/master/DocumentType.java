package id.perumdamts.mail.entity.master;

import id.perumdamts.mail.entity.SqidEntity;
import id.perumdamts.mail.enums.RecordStatusActive;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "jenis_dokumen")
@SQLRestriction("is_deleted = 0")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DocumentType implements SqidEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "jenis_dokumen", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_new", nullable = false)
    private RecordStatusActive status = RecordStatusActive.ACTIVE;

    @Column(name = "is_deleted", nullable = false)
    private Boolean deleted = false;

    @OneToMany(mappedBy = "documentType")
    private java.util.List<id.perumdamts.mail.entity.core.Publication> publications;

    public DocumentType(String name) {
        this.name = name;
        this.status = RecordStatusActive.ACTIVE;
        this.deleted = false;
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

    public boolean isActive() {
        return !this.deleted && this.status == RecordStatusActive.ACTIVE;
    }
}
