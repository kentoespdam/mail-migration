package id.perumdamts.mail.entity.core;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity untuk print log - mencatat setiap kali surat dicetak.
 * Digunakan untuk verifikasi keaslian dokumen cetak.
 */
@Entity
@Table(name = "print_log", indexes = {
        @Index(name = "idx_print_log_auth_code", columnList = "auth_code"),
        @Index(name = "idx_print_log_mail_id", columnList = "m_id")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PrintLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "mail_id", nullable = false)
    private Long mailId;

    @Column(name = "auth_code", length = 32, nullable = false, unique = true)
    private String authCode;

    @Column(name = "username", length = 128, nullable = false)
    private String username;

    @Column(name = "date", nullable = false)
    private LocalDateTime printDate;

    @Column(name = "ip_address", length = 32)
    private String ipAddress;

    @Column(name = "verify_log", length = 64)
    private String verifyLog;

    /**
     * Factory method untuk create print log dengan auth code.
     */
    public static PrintLog create(Long mailId, String username, String ipAddress) {
        PrintLog printLog = new PrintLog();
        printLog.mailId = mailId;
        printLog.username = username;
        printLog.printDate = LocalDateTime.now();
        printLog.ipAddress = ipAddress;
        return printLog;
    }

    /**
     * Generate verification URL untuk dokumen ini.
     */
    public String getVerificationUrl(String baseUrl) {
        return baseUrl + "/api/mails/verify-sign/" + authCode;
    }

    public void recordVerification(String ipAddress) {
        if (this.verifyLog == null) {
            this.verifyLog = ipAddress;
        } else if (!this.verifyLog.contains(ipAddress)) {
            this.verifyLog += "," + ipAddress;
        }
    }
}
