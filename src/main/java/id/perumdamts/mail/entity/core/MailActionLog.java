package id.perumdamts.mail.entity.core;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity untuk mencatat riwayat aksi pada surat (Audit Trail).
 */
@Entity
@Table(name = "mail_action_log", indexes = {
        @Index(name = "idx_mail_action_log_mail_id", columnList = "mail_id")
})
@NoArgsConstructor
@Getter
@Setter
public class MailActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mail_id", nullable = false)
    private Long mailId;

    @Column(name = "action", nullable = false, length = 32)
    private String action;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "old_value", length = 100)
    private String oldValue;

    @Column(name = "new_value", length = 100)
    private String newValue;

    public MailActionLog(Long mailId, String action, String username, String description) {
        this.mailId = mailId;
        this.action = action;
        this.username = username;
        this.description = description;
    }
}
