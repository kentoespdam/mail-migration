package id.perumdamts.mail.repository.jpa;

import id.perumdamts.mail.domain.entity.QuickMessage;
import id.perumdamts.mail.domain.enums.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QuickMessageRepository extends JpaRepository<QuickMessage, Integer> {

    List<QuickMessage> findAllByStatusOrderByMessageAsc(RecordStatus status);

    Page<QuickMessage> findAllByOrderByMessageAsc(Pageable pageable);

    boolean existsByMessage(String message);

    boolean existsByMessageAndIdNot(String message, Integer id);
}
