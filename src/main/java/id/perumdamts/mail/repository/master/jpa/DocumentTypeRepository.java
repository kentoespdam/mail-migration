package id.perumdamts.mail.repository.master.jpa;

import id.perumdamts.mail.entity.master.DocumentType;
import id.perumdamts.mail.enums.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface DocumentTypeRepository extends JpaRepository<DocumentType, Long>,
                                                JpaSpecificationExecutor<DocumentType> {
    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    List<DocumentType> findAllByStatusOrderByIdAsc(RecordStatus status);
}
