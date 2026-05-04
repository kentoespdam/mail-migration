package id.perumdamts.mail.event;

import id.perumdamts.mail.entity.core.MailRecipient;
import id.perumdamts.mail.repository.core.jpa.MailRecipientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipientReadStatusListenerTest {

    @Mock
    private MailRecipientRepository recipientRepository;

    @InjectMocks
    private RecipientReadStatusListener listener;

    private MailRecipient recipient;

    @BeforeEach
    void setUp() {
        recipient = mock(MailRecipient.class);
    }

    @Test
    void handleRecipientRead_whenRecipientExistsAndNotNotified_marksAsNotified() {
        Long mailId = 1L;
        Long userId = 100L;
        RecipientReadEvent event = new RecipientReadEvent(mailId, userId);

        when(recipientRepository.findByMailIdAndUserId(mailId, userId)).thenReturn(Optional.of(recipient));
        when(recipient.isNotified()).thenReturn(false);

        listener.handleRecipientRead(event);

        verify(recipient).markNotified();
        verify(recipientRepository).save(recipient);
    }

    @Test
    void handleRecipientRead_whenRecipientAlreadyNotified_doesNothing() {
        Long mailId = 1L;
        Long userId = 100L;
        RecipientReadEvent event = new RecipientReadEvent(mailId, userId);

        when(recipientRepository.findByMailIdAndUserId(mailId, userId)).thenReturn(Optional.of(recipient));
        when(recipient.isNotified()).thenReturn(true);

        listener.handleRecipientRead(event);

        verify(recipient, never()).markNotified();
        verify(recipientRepository, never()).save(recipient);
    }

    @Test
    void handleRecipientRead_whenRecipientNotFound_doesNothing() {
        Long mailId = 1L;
        Long userId = 100L;
        RecipientReadEvent event = new RecipientReadEvent(mailId, userId);

        when(recipientRepository.findByMailIdAndUserId(mailId, userId)).thenReturn(Optional.empty());

        listener.handleRecipientRead(event);

        verify(recipientRepository, never()).save(any());
    }
}
