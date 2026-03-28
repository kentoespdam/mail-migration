package id.perumdamts.mail.entity.core;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "mail_archive_access")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MailArchiveAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "mail_archive_id")
    private Long archiveId;

    @Column(name = "position_id")
    private Integer positionId;

    @Column(name = "access_level")
    private Integer accessLevel;

    @Column(name = "granted_date")
    private LocalDateTime grantedDate;

    @Column(name = "granted_by")
    private Integer grantedBy;

    public static MailArchiveAccess create(Long archiveId, Integer positionId,
                                            Integer accessLevel, Integer grantedBy) {
        var access = new MailArchiveAccess();
        access.setArchiveId(archiveId);
        access.setPositionId(positionId);
        access.setAccessLevel(accessLevel);
        access.setGrantedBy(grantedBy);
        access.setGrantedDate(LocalDateTime.now());
        return access;
    }
}
