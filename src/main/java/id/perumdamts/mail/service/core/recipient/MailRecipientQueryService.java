package id.perumdamts.mail.service.core.recipient;

import id.perumdamts.mail.config.CacheConfig;
import id.perumdamts.mail.dto.core.mail.MailTrackingResponse;
import id.perumdamts.mail.dto.core.mail.RecipientReadStatusResponse;
import id.perumdamts.mail.dto.core.recipient.RecipientResponse;
import id.perumdamts.mail.repository.core.jooq.RecipientQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MailRecipientQueryService {

    private final RecipientQueryRepository recipientQueryRepository;

    public List<RecipientResponse> findRecipients(Long mailId) {
        return recipientQueryRepository.findByMailId(mailId);
    }

    @Cacheable(value = CacheConfig.CacheNames.MAIL_TRACKING, key = "#mailId")
    public List<MailTrackingResponse> findTracking(Long mailId) {
        return recipientQueryRepository.findTracking(mailId);
    }

    @Cacheable(value = CacheConfig.CacheNames.MAIL_READ_STATUS, key = "#mailId")
    public List<RecipientReadStatusResponse> findReadStatus(Long mailId) {
        return recipientQueryRepository.findReadStatus(mailId);
    }
}
