package id.perumdamts.mail.repository.master.jpa;

import id.perumdamts.mail.entity.master.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface DocumentTypeRepository extends JpaRepository<DocumentType, Integer>, JpaSpecificationExecutor<DocumentType> {

    List<DocumentType> findByStatus(Integer status);
}
