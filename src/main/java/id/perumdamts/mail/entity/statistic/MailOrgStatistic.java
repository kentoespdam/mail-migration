package id.perumdamts.mail.entity.statistic;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity untuk tabel {@code mail_org_statistic}.
 * Agregasi total surat per bulan per organisasi (unit).
 */
@Entity
@Table(name = "mail_org_statistic", indexes = {
        @Index(name = "idx_org_stat_period_org", columnList = "period_month, created_by_org")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MailOrgStatistic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "period_month", nullable = false)
    private Integer periodMonth; // Format YYYYMM

    @Column(name = "created_by_org", nullable = false)
    private Integer organizationId;

    @Column(name = "total")
    private Integer total = 0;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
