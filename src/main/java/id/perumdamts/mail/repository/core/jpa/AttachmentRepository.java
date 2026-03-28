package id.perumdamts.mail.repository.core.jpa;

import id.perumdamts.mail.entity.core.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Integer> {

    List<Attachment> findAllByRefTypeAndRefId(Integer refType, Long refId);
}
