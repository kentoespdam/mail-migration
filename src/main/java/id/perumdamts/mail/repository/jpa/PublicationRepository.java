package id.perumdamts.mail.repository.jpa;

import id.perumdamts.mail.domain.entity.Publication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublicationRepository extends JpaRepository<Publication, Integer> {

    List<Publication> findByNotifFlag(int notifFlag);
}
