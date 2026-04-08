package id.perumdamts.mail.service.core.recipient;

import id.perumdamts.mail.dto.core.recipient.RecipientMapper;
import id.perumdamts.mail.dto.core.recipient.RecipientResponse;
import id.perumdamts.mail.repository.core.jpa.MailRecipientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MailRecipientQueryService {

    private final MailRecipientRepository recipientRepository;
    private final RecipientMapper recipientMapper;

    public MailRecipientQueryService(MailRecipientRepository recipientRepository,
            RecipientMapper recipientMapper) {
        this.recipientRepository = recipientRepository;
        this.recipientMapper = recipientMapper;
    }

    public List<RecipientResponse> getRecipients(Long mailId) {
        return recipientRepository.findByMailId(mailId).stream()
                .map(recipientMapper::toResponse)
                .toList();
    }
}
