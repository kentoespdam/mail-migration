package id.perumdamts.mail.repository.core.jpa;

import id.perumdamts.mail.entity.core.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    Optional<Attachment> findByIdAndRefIdAndRefType(Long id, Long refId, Integer refType);
}
