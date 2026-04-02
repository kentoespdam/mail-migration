package id.perumdamts.mail.entity.master;

import id.perumdamts.mail.entity.SqidEntity;
import id.perumdamts.mail.enums.RecordStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "jenis_dokumen")
@SQLRestriction("status <> 'DELETED'")
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
    private RecordStatus status = RecordStatus.ACTIVE;

    @OneToMany(mappedBy = "documentType")
    private java.util.List<id.perumdamts.mail.entity.core.Publication> publications;

    public DocumentType(String name) {
        this.name = name;
        this.status = RecordStatus.ACTIVE;
    }

    public void markDeleted() {
        this.status = RecordStatus.DELETED;
    }

    public boolean isActive() {
        return this.status == RecordStatus.ACTIVE;
    }
}
