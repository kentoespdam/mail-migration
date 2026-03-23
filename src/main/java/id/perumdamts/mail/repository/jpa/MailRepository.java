package id.perumdamts.mail.repository.jpa;

import id.perumdamts.mail.domain.entity.Mail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MailRepository extends JpaRepository<Mail, Integer> {
}
