package id.perumdamts.mail.entity.core;

import id.perumdamts.mail.entity.master.MailCategory;
import id.perumdamts.mail.entity.master.MailType;
import id.perumdamts.mail.enums.MailStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "mail")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Mail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "m_id")
    private Integer id;

    @Column(name = "m_no", length = 100)
    private String mailNumber;

    @Column(name = "m_date")
    private LocalDate mailDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "m_type")
    private MailType mailType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "m_category")
    private MailCategory mailCategory;

    @Column(name = "m_subject")
    private String subject;

    @Column(name = "m_content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "m_note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "m_max_response_date")
    private LocalDate maxResponseDate;

    @Column(name = "m_status", nullable = false)
    private Integer status = MailStatus.DRAFT.getDbValue();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "m_root_id")
    private Mail rootMail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "m_parent_id")
    private Mail parentMail;

    @Column(name = "m_attachment_qty")
    private Integer attachmentQty = 0;

    @Column(name = "m_to_str", columnDefinition = "TEXT")
    private String toStr;

    @Column(name = "m_created_date")
    private LocalDateTime createdDate;

    @Column(name = "m_updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "m_created_by")
    private Integer createdBy;

    @Column(name = "m_created_by_name", length = 100)
    private String createdByName;

    @Column(name = "m_no_surat_masuk", length = 100)
    private String noSuratMasuk;

    @Column(name = "m_asal_surat_masuk", length = 200)
    private String asalSuratMasuk;

    @Column(name = "m_tgl_surat_masuk", length = 50)
    private String tglSuratMasuk;

    @Column(name = "m_tujuan_surat_keluar", length = 200)
    private String tujuanSuratKeluar;

    @Column(name = "m_penerima_surat_keluar", length = 200)
    private String penerimaSuratKeluar;

    // ── Domain Methods ──

    public MailStatus getMailStatus() {
        return MailStatus.fromDbValue(this.status);
    }

    public boolean isDraft() {
        return this.status == MailStatus.DRAFT.getDbValue();
    }

    public boolean isSent() {
        return this.status == MailStatus.SENT.getDbValue();
    }

    public static String buildToStr(List<MailRecipient> recipients) {
        return recipients.stream()
                .map(r -> r.getEmpName() != null ? r.getEmpName() : "User#" + r.getUserId())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    public void send(String mailNumber) {
        this.mailNumber = mailNumber;
        this.status = MailStatus.SENT.getDbValue();
        this.mailDate = LocalDate.now();
        this.updatedDate = LocalDateTime.now();
    }
}
