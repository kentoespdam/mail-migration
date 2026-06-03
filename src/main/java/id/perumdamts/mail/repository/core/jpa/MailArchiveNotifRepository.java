package id.perumdamts.mail.repository.core.jpa;

import id.perumdamts.mail.entity.core.MailArchiveNotif;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MailArchiveNotifRepository extends JpaRepository<MailArchiveNotif, Integer> {
    List<MailArchiveNotif> findByMailArchiveId(Long mailArchiveId);
}
