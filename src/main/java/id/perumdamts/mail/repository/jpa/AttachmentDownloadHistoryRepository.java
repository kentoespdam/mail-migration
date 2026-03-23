package id.perumdamts.mail.repository.jpa;

import id.perumdamts.mail.domain.entity.AttachmentDownloadHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentDownloadHistoryRepository extends JpaRepository<AttachmentDownloadHistory, Integer> {
}
