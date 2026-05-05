package id.perumdamts.mail.service.core.archive;

import id.perumdamts.mail.dto.core.archive.MailArchiveNotifResponse;
import id.perumdamts.mail.entity.core.MailArchiveNotif;
import id.perumdamts.mail.repository.core.jpa.MailArchiveNotifRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class MailArchiveNotifCommandService {
    private final MailArchiveNotifRepository repository;

    public MailArchiveNotifCommandService(MailArchiveNotifRepository repository) {
        this.repository = repository;
    }

    public void markAsProcessed(Integer id) {
        MailArchiveNotif notif = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("MailArchiveNotif not found: " + id));
        notif.setProcessedDate(LocalDateTime.now());
        repository.save(notif);
    }

    public void create(Long archiveId, Integer flag) {
        MailArchiveNotif notif = new MailArchiveNotif();
        notif.setMailArchiveId(archiveId);
        notif.setNotifFlag(flag);
        notif.setInsertDate(LocalDateTime.now());
        repository.save(notif);
    }
}
