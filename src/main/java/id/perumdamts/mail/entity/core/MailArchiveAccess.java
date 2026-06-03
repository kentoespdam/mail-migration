package id.perumdamts.mail.entity.core;

import id.perumdamts.mail.util.BooleanYesNoConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Column(name = "pos_id")
    private Integer positionId;

    @Convert(converter = BooleanYesNoConverter.class)
    @Column(name = "access", length = 1)
    private Boolean canAccess = true;

    @Convert(converter = BooleanYesNoConverter.class)
    @Column(name = "download", length = 1)
    private Boolean canDownload = true;

    @Convert(converter = BooleanYesNoConverter.class)
    @Column(name = "history", length = 1)
    private Boolean canViewHistory = true;

    public static MailArchiveAccess create(Long archiveId, Integer positionId,
                                            Boolean canAccess, Boolean canDownload, Boolean canViewHistory) {
        var access = new MailArchiveAccess();
        access.setArchiveId(archiveId);
        access.setPositionId(positionId);
        access.setCanAccess(canAccess);
        access.setCanDownload(canDownload);
        access.setCanViewHistory(canViewHistory);
        return access;
    }
}
