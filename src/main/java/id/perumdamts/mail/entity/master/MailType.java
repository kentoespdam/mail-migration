package id.perumdamts.mail.entity.master;

import id.perumdamts.mail.entity.SqidEntity;
import id.perumdamts.mail.enums.RecordStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mail_type")
@SQLRestriction("mail_type_status != 'DELETED'")
@NoArgsConstructor
@Getter
@Setter
public class MailType implements SqidEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mail_type_id")
    private Long id;

    @Column(name = "mail_type", nullable = false, length = 32)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "mail_type_status", nullable = false)
    private RecordStatus status = RecordStatus.ACTIVE;

    @OneToMany(mappedBy = "mailType", fetch = FetchType.LAZY)
    private List<MailCategory> categories = new ArrayList<>();

    public MailType(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return status == RecordStatus.ACTIVE;
    }

    public void markDeleted() {
        this.status = RecordStatus.DELETED;
    }

    public void toggleStatus() {
        if (status == RecordStatus.DELETED) {
            throw new IllegalStateException("Cannot toggle status of a DELETED record");
        }
        this.status = (status == RecordStatus.ACTIVE) ? RecordStatus.INACTIVE : RecordStatus.ACTIVE;
    }
}
