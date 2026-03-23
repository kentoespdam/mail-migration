package id.perumdamts.mail.repository.jpa;

import id.perumdamts.mail.domain.entity.QuickMessage;
import id.perumdamts.mail.domain.enums.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuickMessageRepository extends JpaRepository<QuickMessage, Integer> {

    List<QuickMessage> findAllByStatus(RecordStatus status);
}
