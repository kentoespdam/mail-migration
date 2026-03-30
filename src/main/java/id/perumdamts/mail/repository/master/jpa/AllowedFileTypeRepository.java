package id.perumdamts.mail.repository.master.jpa;

import id.perumdamts.mail.entity.master.AllowedFileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AllowedFileTypeRepository extends JpaRepository<AllowedFileType, Integer>, JpaSpecificationExecutor<AllowedFileType> {

    List<AllowedFileType> findByContextAndIsActiveTrue(String context);
}
