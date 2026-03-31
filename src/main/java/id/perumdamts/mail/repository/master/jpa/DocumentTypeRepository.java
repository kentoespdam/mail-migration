package id.perumdamts.mail.repository.master.jpa;

import id.perumdamts.mail.entity.master.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DocumentTypeRepository extends JpaRepository<DocumentType, Integer>, JpaSpecificationExecutor<DocumentType> {
}
