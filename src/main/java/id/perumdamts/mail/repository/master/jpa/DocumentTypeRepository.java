package id.perumdamts.mail.repository.master.jpa;

import id.perumdamts.mail.entity.master.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentTypeRepository extends JpaRepository<DocumentType, Integer> {

    List<DocumentType> findByStatus(Integer status);
}
