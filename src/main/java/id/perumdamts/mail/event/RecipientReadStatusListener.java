package id.perumdamts.mail.event;

import id.perumdamts.mail.repository.core.jpa.MailRecipientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecipientReadStatusListener {

    private final MailRecipientRepository recipientRepository;

    @EventListener
    @Transactional
    public void handleRecipientRead(RecipientReadEvent event) {
        log.debug("Marking recipient as notified for mailId: {} and userId: {}", event.mailId(), event.userId());
        recipientRepository.findByMailIdAndUserId(event.mailId(), event.userId())
                .ifPresent(recipient -> {
                    if (!recipient.isNotified()) {
                        recipient.markNotified();
                        recipientRepository.save(recipient);
                        log.debug("Recipient id: {} marked as notified", recipient.getId());
                    }
                });
    }
}
