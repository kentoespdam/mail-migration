package id.perumdamts.mail.repository.jpa;

import id.perumdamts.mail.domain.entity.MailType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MailTypeRepository extends JpaRepository<MailType, Integer> {

    Optional<MailType> findByName(String name);

    boolean existsByName(String name);
}
