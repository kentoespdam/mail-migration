package id.perumdamts.mail.entity.core;

import id.perumdamts.mail.entity.master.MailCategory;
import id.perumdamts.mail.entity.master.MailType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity untuk tabel {@code mail_respontime}.
 * Mencatat performa waktu respon antar surat (reply).
 */
@Entity
@Table(name = "mail_respontime")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MailResponseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orig_m_id")
    private Mail originalMail;

    @Column(name = "orig_date")
    private LocalDateTime originalDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_m_id")
    private Mail replyMail;

    @Column(name = "reply_date")
    private LocalDateTime replyDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "m_type")
    private MailType mailType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "m_category")
    private MailCategory mailCategory;

    @Column(name = "respon_time")
    private Integer responseTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
