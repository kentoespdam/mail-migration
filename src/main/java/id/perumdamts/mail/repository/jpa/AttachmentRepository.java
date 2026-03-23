package id.perumdamts.mail.repository.jpa;

import id.perumdamts.mail.domain.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Integer> {

    List<Attachment> findAllByRefTypeAndRefId(Integer refType, Long refId);
}
