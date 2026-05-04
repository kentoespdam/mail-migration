package id.perumdamts.mail.repository.master.jpa;

import id.perumdamts.mail.entity.master.MessageTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageTemplateRepository extends JpaRepository<MessageTemplate, Long> {
}
