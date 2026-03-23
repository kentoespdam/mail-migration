package id.perumdamts.mail.domain.entity;

import id.perumdamts.mail.domain.enums.CirculationType;
import jakarta.persistence.*;

@Entity
@Table(name = "mail_recipient", uniqueConstraints = {
        @UniqueConstraint(name = "uq_recipient_mail_user", columnNames = {"mail_id", "user_id"})
})
public class MailRecipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mail_id", nullable = false)
    private Mail mail;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "emp_id", nullable = false)
    private Integer empId;

    @Column(name = "emp_name", length = 100)
    private String empName;

    @Column(name = "pos_id")
    private Integer posId;

    @Column(name = "pos_name", length = 100)
    private String posName;

    @Column(name = "circulation", nullable = false)
    private Integer circulation;

    @Column(name = "email", nullable = false)
    private Integer emailNotif = 0;

    @Column(name = "sms", nullable = false)
    private Integer smsNotif = 0;

    protected MailRecipient() {}

    public MailRecipient(Mail mail, Integer userId, Integer empId, CirculationType circulationType) {
        this.mail = mail;
        this.userId = userId;
        this.empId = empId;
        this.circulation = circulationType.getDbValue();
    }

    // ── Domain Methods ──

    public CirculationType getCirculationType() {
        return CirculationType.fromDbValue(this.circulation);
    }

    // ── Getters & Setters ──

    public Long getId() { return id; }

    public Mail getMail() { return mail; }

    public Integer getUserId() { return userId; }

    public Integer getEmpId() { return empId; }

    public String getEmpName() { return empName; }
    public void setEmpName(String empName) { this.empName = empName; }

    public Integer getPosId() { return posId; }
    public void setPosId(Integer posId) { this.posId = posId; }

    public String getPosName() { return posName; }
    public void setPosName(String posName) { this.posName = posName; }

    public Integer getCirculation() { return circulation; }

    public Integer getEmailNotif() { return emailNotif; }
    public void setEmailNotif(Integer emailNotif) { this.emailNotif = emailNotif; }

    public Integer getSmsNotif() { return smsNotif; }
    public void setSmsNotif(Integer smsNotif) { this.smsNotif = smsNotif; }
}
