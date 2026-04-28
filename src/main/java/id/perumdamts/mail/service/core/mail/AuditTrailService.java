package id.perumdamts.mail.service.core.mail;

import id.perumdamts.mail.entity.core.MailActionLog;
import id.perumdamts.mail.repository.core.jpa.MailActionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service untuk pencatatan audit trail secara asinkron.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditTrailService {

    private final MailActionLogRepository repository;

    @Async
    public void logAction(Long mailId, String action, String username, String description) {
        logAction(mailId, action, username, description, null, null);
    }

    @Async
    public void logAction(Long mailId, String action, String username, String description, String oldValue, String newValue) {
        try {
            MailActionLog actionLog = new MailActionLog(mailId, action, username, description);
            actionLog.setOldValue(oldValue);
            actionLog.setNewValue(newValue);
            repository.save(actionLog);
        } catch (Exception e) {
            log.error("Failed to save audit log for mail {}: {}", mailId, e.getMessage());
        }
    }
}
