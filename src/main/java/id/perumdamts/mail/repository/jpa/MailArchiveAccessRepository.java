package id.perumdamts.mail.repository.jpa;

import id.perumdamts.mail.domain.entity.MailArchiveAccess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MailArchiveAccessRepository extends JpaRepository<MailArchiveAccess, Integer> {

    List<MailArchiveAccess> findByArchiveId(Long archiveId);

    void deleteByArchiveId(Long archiveId);
}
