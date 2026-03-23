package id.perumdamts.mail.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity untuk tabel {@code attachment_download_history}.
 * Audit trail tiap kali attachment didownload.
 */
@Entity
@Table(name = "attachment_download_history")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AttachmentDownloadHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "attachment_id", nullable = false)
    private Integer attachmentId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "emp_name", length = 64)
    private String empName;

    @Column(name = "emp_pos_name", length = 64)
    private String empPosName;

    @Column(name = "download_time", nullable = false)
    private LocalDateTime downloadTime;

    public AttachmentDownloadHistory(Integer attachmentId, Integer userId,
                                     String empName, String empPosName) {
        this.attachmentId = attachmentId;
        this.userId = userId;
        this.empName = empName;
        this.empPosName = empPosName;
        this.downloadTime = LocalDateTime.now();
    }
}
