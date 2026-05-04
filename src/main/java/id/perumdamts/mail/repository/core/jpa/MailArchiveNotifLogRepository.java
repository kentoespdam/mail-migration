package id.perumdamts.mail.repository.core.jpa;

import id.perumdamts.mail.entity.core.MailArchiveNotifLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MailArchiveNotifLogRepository extends JpaRepository<MailArchiveNotifLog, Long> {
    List<MailArchiveNotifLog> findByArchiveId(Long archiveId);
}
