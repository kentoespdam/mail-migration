package id.perumdamts.mail.domain.entity;

import id.perumdamts.mail.domain.enums.MailStatus;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "mail")
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

    public Mail() {}

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

    public void send(String mailNumber) {
        this.mailNumber = mailNumber;
        this.status = MailStatus.SENT.getDbValue();
        this.createdDate = LocalDateTime.now();
    }

    // ── Getters & Setters ──

    public Integer getId() { return id; }

    public String getMailNumber() { return mailNumber; }
    public void setMailNumber(String mailNumber) { this.mailNumber = mailNumber; }

    public LocalDate getMailDate() { return mailDate; }
    public void setMailDate(LocalDate mailDate) { this.mailDate = mailDate; }

    public MailType getMailType() { return mailType; }
    public void setMailType(MailType mailType) { this.mailType = mailType; }

    public MailCategory getMailCategory() { return mailCategory; }
    public void setMailCategory(MailCategory mailCategory) { this.mailCategory = mailCategory; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDate getMaxResponseDate() { return maxResponseDate; }
    public void setMaxResponseDate(LocalDate maxResponseDate) { this.maxResponseDate = maxResponseDate; }

    public Integer getStatus() { return status; }

    public Mail getRootMail() { return rootMail; }
    public void setRootMail(Mail rootMail) { this.rootMail = rootMail; }

    public Mail getParentMail() { return parentMail; }
    public void setParentMail(Mail parentMail) { this.parentMail = parentMail; }

    public Integer getAttachmentQty() { return attachmentQty; }
    public void setAttachmentQty(Integer attachmentQty) { this.attachmentQty = attachmentQty; }

    public String getToStr() { return toStr; }
    public void setToStr(String toStr) { this.toStr = toStr; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public String getNoSuratMasuk() { return noSuratMasuk; }
    public void setNoSuratMasuk(String noSuratMasuk) { this.noSuratMasuk = noSuratMasuk; }

    public String getAsalSuratMasuk() { return asalSuratMasuk; }
    public void setAsalSuratMasuk(String asalSuratMasuk) { this.asalSuratMasuk = asalSuratMasuk; }

    public String getTglSuratMasuk() { return tglSuratMasuk; }
    public void setTglSuratMasuk(String tglSuratMasuk) { this.tglSuratMasuk = tglSuratMasuk; }

    public String getTujuanSuratKeluar() { return tujuanSuratKeluar; }
    public void setTujuanSuratKeluar(String tujuanSuratKeluar) { this.tujuanSuratKeluar = tujuanSuratKeluar; }

    public String getPenerimaSuratKeluar() { return penerimaSuratKeluar; }
    public void setPenerimaSuratKeluar(String penerimaSuratKeluar) { this.penerimaSuratKeluar = penerimaSuratKeluar; }
}
