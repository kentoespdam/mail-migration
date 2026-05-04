package id.perumdamts.mail.entity.statistic;

import id.perumdamts.mail.entity.SqidEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "mail_org_statistic", indexes = @Index(name = "idx_org_stat_period_org", columnList = "period_month, created_by_org"))
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MailOrgStatistic implements SqidEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "period_month", nullable = false)
    private Integer periodMonth;

    @Column(name = "created_by_org", nullable = false)
    private Integer createdByOrg;

    @Column(name = "total", nullable = false)
    private Integer total;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}