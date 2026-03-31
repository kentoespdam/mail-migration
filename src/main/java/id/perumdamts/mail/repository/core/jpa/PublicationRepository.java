package id.perumdamts.mail.repository.core.jpa;

import id.perumdamts.mail.entity.core.Publication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublicationRepository extends JpaRepository<Publication, Long> {

    List<Publication> findByNotifFlag(int notifFlag);
}
