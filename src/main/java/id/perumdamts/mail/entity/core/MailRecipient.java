package id.perumdamts.mail.entity.core;

import id.perumdamts.mail.enums.CirculationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mail_recipient", uniqueConstraints = {
        @UniqueConstraint(name = "uq_recipient_mail_user", columnNames = {"mail_id", "user_id"})
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
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

    @Column(name = "is_notified", nullable = false)
    private Boolean notified = false;

    @Column(name = "is_read", nullable = false)
    private Boolean read = false;

    @Column(name = "folder_position")
    private Integer folderPosition;


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

    public Boolean isNotified() {
        return notified;
    }

    public Boolean isRead() {
        return read;
    }

    public void markNotified() {
        this.notified = true;
    }

    public void markRead() {
        this.read = true;
    }
}
