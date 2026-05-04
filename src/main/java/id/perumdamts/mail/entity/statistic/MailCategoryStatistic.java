package id.perumdamts.mail.entity.statistic;

import id.perumdamts.mail.entity.SqidEntity;
import id.perumdamts.mail.entity.master.MailCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "mail_category_statistic", indexes = {
        @Index(name = "idx_mcs_period_category", columnList = "period_month, category_id")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MailCategoryStatistic implements SqidEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "period_month")
    private Integer periodMonth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", foreignKey = @ForeignKey(name = "fk_mcs_category"))
    private MailCategory category;

    @Column(name = "total")
    private Integer total;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
