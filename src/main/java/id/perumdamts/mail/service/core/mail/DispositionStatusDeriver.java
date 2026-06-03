package id.perumdamts.mail.service.core.mail;

import id.perumdamts.mail.config.CacheConfig;

import id.perumdamts.mail.entity.core.Mail;
import id.perumdamts.mail.enums.DispositionStatus;
import id.perumdamts.mail.repository.core.jpa.MailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DispositionStatusDeriver {

    private final MailRepository mailRepository;

    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(
        value = CacheConfig.CacheNames.DISPOSISI_STATUS,
        key = "T(java.lang.String).valueOf(#mailId)"
    )
    public DispositionStatusResult deriveStatus(Long mailId) {
        Mail mail = mailRepository.findById(mailId).orElse(null);
        if (mail == null || mail.getDeleted()) {
            return null;
        }

        Long rootId = resolveRootId(mail);
        int depth = calculateDepth(mail);

        List<Mail> children = mailRepository.findByRootIdAndDeletedFalse(rootId);
        DispositionStatus status = computeStatus(rootId, children);

        return new DispositionStatusResult(status, mail.getMaxResponseDate(), depth);
    }

    private Long resolveRootId(Mail mail) {
        if (mail.getRootMail() != null) {
            return mail.getRootMail().getId();
        }
        return mail.getId();
    }

    private int calculateDepth(Mail mail) {
        int depth = 0;
        Mail current = mail;
        while (current.getParentMail() != null && !current.getId().equals(current.getParentMail().getId())) {
            depth++;
            current = current.getParentMail();
            if (depth > 20) break;
        }
        return depth;
    }

    private DispositionStatus computeStatus(Long rootId, List<Mail> children) {
        if (children == null || children.isEmpty()) {
            return DispositionStatus.PENDING;
        }

        boolean hasRepliedChild = children.stream()
                .anyMatch(child -> child.getParentMail() != null && 
                                   !child.getId().equals(rootId));

        if (hasRepliedChild) {
            boolean allReplied = children.stream()
                    .allMatch(child -> child.getParentMail() != null && 
                                      !child.getId().equals(rootId));
            return allReplied ? DispositionStatus.DONE : DispositionStatus.IN_PROGRESS;
        }

        return DispositionStatus.IN_PROGRESS;
    }

    public record DispositionStatusResult(
            DispositionStatus status,
            java.time.LocalDate deadline,
            int depth
    ) {}
}
