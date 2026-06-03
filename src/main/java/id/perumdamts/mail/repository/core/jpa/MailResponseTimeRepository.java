package id.perumdamts.mail.repository.core.jpa;

import id.perumdamts.mail.entity.core.MailResponseTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MailResponseTimeRepository extends JpaRepository<MailResponseTime, Integer> {
    Optional<MailResponseTime> findByOriginalMailId(Long originalMailId);
    Optional<MailResponseTime> findByReplyMailId(Long replyMailId);
}
