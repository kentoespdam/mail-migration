package id.perumdamts.mail.repository.core.jpa;

import id.perumdamts.mail.entity.core.Mail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MailRepository extends JpaRepository<Mail, Integer> {
}
