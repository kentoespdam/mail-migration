package id.perumdamts.mail.repository.core.jpa;

import id.perumdamts.mail.entity.core.MailActionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MailActionLogRepository extends JpaRepository<MailActionLog, Long> {
    List<MailActionLog> findAllByMailIdOrderByCreatedAtDesc(Long mailId);
}
