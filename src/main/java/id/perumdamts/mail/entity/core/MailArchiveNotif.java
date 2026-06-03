package id.perumdamts.mail.entity.core;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity untuk tabel {@code mail_archive_notif}.
 * Queue untuk notifikasi arsip surat baru.
 */
@Entity
@Table(name = "mail_archive_notif")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MailArchiveNotif {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "mail_archive_id", nullable = false)
    private Long mailArchiveId;

    @Column(name = "notif_flag")
    private Integer notifFlag;

    @Column(name = "insert_date")
    private LocalDateTime insertDate;

    @Column(name = "processed_date")
    private LocalDateTime processedDate;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
