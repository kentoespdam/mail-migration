package id.perumdamts.mail.repository.jpa;

import id.perumdamts.mail.domain.entity.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentTypeRepository extends JpaRepository<DocumentType, Integer> {

    List<DocumentType> findByStatus(Integer status);
}
