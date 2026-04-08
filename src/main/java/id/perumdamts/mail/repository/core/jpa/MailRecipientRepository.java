package id.perumdamts.mail.repository.core.jpa;

import id.perumdamts.mail.entity.core.MailRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface MailRecipientRepository extends JpaRepository<MailRecipient, Long> {

    List<MailRecipient> findByMailId(Long mailId);

    boolean existsByMailIdAndUserId(Long mailId, Long userId);

    void deleteByMailIdAndId(Long mailId, Long id);

    @Query("SELECT r.userId FROM MailRecipient r WHERE r.mail.id = :mailId")
    Set<Long> findUserIdsByMailId(@Param("mailId") Long mailId);

    void deleteAllByMailIdAndIdIn(Long mailId, List<Long> ids);

    void deleteByMailId(Long mailId);
}
