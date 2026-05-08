package id.perumdamts.mail.repository.core.jpa;

import id.perumdamts.mail.entity.core.Mail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MailRepository extends JpaRepository<Mail, Long> {
    @Query("SELECT m FROM Mail m LEFT JOIN FETCH m.mailType LEFT JOIN FETCH m.mailCategory WHERE m.id = :id")
    Optional<Mail> findByIdWithDetails(Long id);

    @Query("SELECT m FROM Mail m WHERE m.rootMail.id = :rootId AND m.deleted = false AND m.id <> :rootId")
    List<Mail> findByRootIdAndDeletedFalse(Long rootId);
}
