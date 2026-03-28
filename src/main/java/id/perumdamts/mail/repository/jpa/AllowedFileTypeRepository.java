package id.perumdamts.mail.repository.jpa;

import id.perumdamts.mail.domain.entity.AllowedFileType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AllowedFileTypeRepository extends JpaRepository<AllowedFileType, Integer> {

    List<AllowedFileType> findByContextAndIsActiveTrue(String context);
}
