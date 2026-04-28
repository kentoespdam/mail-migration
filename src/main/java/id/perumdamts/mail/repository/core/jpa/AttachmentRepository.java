package id.perumdamts.mail.repository.core.jpa;

import id.perumdamts.mail.entity.core.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Integer> {
    Optional<Attachment> findByIdAndRefIdAndRefType(Integer id, Long refId, Integer refType);

    List<Attachment> findAllByRefTypeAndRefId(Integer refType, Long refId);

    List<Attachment> findByRefTypeAndRefId(Integer refType, Long refId);
}
