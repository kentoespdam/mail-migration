package id.perumdamts.mail.repository.master.jpa;

import id.perumdamts.mail.entity.master.QuickMessage;
import id.perumdamts.mail.enums.RecordStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuickMessageRepository extends JpaRepository<QuickMessage, Integer> {

    List<QuickMessage> findAllByStatusOrderByMessageAsc(RecordStatus status);

    Page<QuickMessage> findAllByOrderByMessageAsc(Pageable pageable);

    Page<QuickMessage> findByMessageContainingIgnoreCaseOrderByMessageAsc(String message, Pageable pageable);

    boolean existsByMessage(String message);

    boolean existsByMessageAndIdNot(String message, Integer id);
}
