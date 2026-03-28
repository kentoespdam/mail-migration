package id.perumdamts.mail.repository.master.jpa;

import id.perumdamts.mail.entity.master.MailType;
import id.perumdamts.mail.enums.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface MailTypeRepository extends JpaRepository<MailType, Integer>,
                                            JpaSpecificationExecutor<MailType> {

    Optional<MailType> findByName(String name);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Integer id);

    List<MailType> findAllByStatusOrderByIdAsc(RecordStatus status);
}
