package id.perumdamts.mail.entity.core;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity untuk tabel {@code mail_archive_notif_log}.
 * Audit trail pengiriman notifikasi arsip.
 */
@Entity
@Table(name = "mail_archive_notif_log")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MailArchiveNotifLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mail_archive_id", nullable = false)
    private MailArchive archive;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "notif_date")
    private LocalDateTime notifDate;

    @PrePersist
    protected void onCreate() {
        this.notifDate = LocalDateTime.now();
    }
}
