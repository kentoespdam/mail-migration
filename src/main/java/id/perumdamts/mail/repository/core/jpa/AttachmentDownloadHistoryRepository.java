package id.perumdamts.mail.repository.core.jpa;

import id.perumdamts.mail.entity.core.AttachmentDownloadHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentDownloadHistoryRepository extends JpaRepository<AttachmentDownloadHistory, Integer> {
}
