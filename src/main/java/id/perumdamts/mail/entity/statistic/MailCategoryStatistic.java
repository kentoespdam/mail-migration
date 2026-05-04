package id.perumdamts.mail.entity.statistic;

import id.perumdamts.mail.entity.master.MailCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity untuk tabel {@code mail_category_statistic}.
 * Agregasi total surat per bulan per kategori.
 */
@Entity
@Table(name = "mail_category_statistic", indexes = {
        @Index(name = "idx_cat_stat_period_cat", columnList = "period_month, category_id")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MailCategoryStatistic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "period_month", nullable = false)
    private Integer periodMonth; // Format YYYYMM

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private MailCategory category;

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
