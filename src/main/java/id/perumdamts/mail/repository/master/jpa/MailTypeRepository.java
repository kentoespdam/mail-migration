package id.perumdamts.mail.repository.master.jpa;

import id.perumdamts.mail.entity.master.MailType;
import id.perumdamts.mail.enums.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface MailTypeRepository extends JpaRepository<MailType, Long>,
                                            JpaSpecificationExecutor<MailType> {
    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    List<MailType> findAllByStatusOrderByIdAsc(RecordStatus status);
}
