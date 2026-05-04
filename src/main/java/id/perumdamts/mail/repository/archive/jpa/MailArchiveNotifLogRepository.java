package id.perumdamts.mail.repository.archive.jpa;

import id.perumdamts.mail.entity.core.MailArchiveNotifLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MailArchiveNotifLogRepository extends JpaRepository<MailArchiveNotifLog, Long> {
}