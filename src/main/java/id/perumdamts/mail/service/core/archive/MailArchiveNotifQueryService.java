package id.perumdamts.mail.service.core.archive;

import id.perumdamts.mail.dto.core.archive.MailArchiveNotifResponse;
import id.perumdamts.mail.repository.core.jooq.MailArchiveNotifQueryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MailArchiveNotifQueryService {
    private final MailArchiveNotifQueryRepository repository;

    public MailArchiveNotifQueryService(MailArchiveNotifQueryRepository repository) {
        this.repository = repository;
    }

    public List<MailArchiveNotifResponse> findPending() {
        return repository.findPending();
    }

    public MailArchiveNotifResponse findById(Integer id) {
        return repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("MailArchiveNotif not found: " + id));
    }
}
