package id.perumdamts.mail.repository.jpa;

import id.perumdamts.mail.domain.entity.MailArchive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MailArchiveRepository extends JpaRepository<MailArchive, Long> {

    @Query("SELECT a FROM MailArchive a WHERE a.id = :id AND a.status != 3")
    Optional<MailArchive> findActiveById(Long id);
}
