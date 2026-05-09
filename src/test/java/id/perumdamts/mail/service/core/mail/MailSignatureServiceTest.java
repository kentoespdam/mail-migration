package id.perumdamts.mail.service.core.mail;

import id.perumdamts.mail.dto.core.mail.MailSignatureVerificationResponse;
import id.perumdamts.mail.entity.core.Mail;
import id.perumdamts.mail.entity.core.PrintLog;
import id.perumdamts.mail.repository.core.jpa.MailArchiveRepository;
import id.perumdamts.mail.repository.core.jpa.MailRepository;
import id.perumdamts.mail.repository.core.jpa.PrintLogRepository;
import id.perumdamts.mail.util.SqidsEncoder;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailSignatureServiceTest {

    @Mock
    private PrintLogRepository printLogRepository;
    @Mock
    private MailRepository mailRepository;
    @Mock
    private MailArchiveRepository mailArchiveRepository;
    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private SqidsEncoder encoder;

    private MailSignatureService service;

    @BeforeEach
    void setUp() {
        service = new MailSignatureService(printLogRepository, mailRepository, mailArchiveRepository, httpServletRequest, encoder);
    }

    @Test
    void verifySignature_WhenMailSoftDeleted_ShouldReturnInvalidResponse() {
        // Arrange
        String authCode = "valid-code";
        PrintLog printLog = mock(PrintLog.class);
        when(printLog.getMailId()).thenReturn(1L);
        when(printLogRepository.findByAuthCode(authCode)).thenReturn(Optional.of(printLog));

        // Mock mailRepository.findById to return empty (simulating soft-delete)
        when(mailRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        MailSignatureVerificationResponse response = service.verifySignature(authCode, "127.0.0.1");

        // Assert
        assertFalse(response.valid());
        assertEquals("INVALID_OR_DELETED", response.message());
    }

    @Test
    void verifySignature_WhenLegacyAuthCode_ShouldReturnSpecialMessage() {
        // Arrange
        String legacyCode = "1234567890abc"; // 13 chars
        when(printLogRepository.findByAuthCode(legacyCode)).thenReturn(Optional.empty());

        // Act
        MailSignatureVerificationResponse response = service.verifySignature(legacyCode, "127.0.0.1");

        // Assert
        assertFalse(response.valid());
        assertEquals("INVALID_OR_DELETED", response.message());
    }

    @Test
    void verifySignature_WhenValid_ShouldReturnValidResponse() {
        // Arrange
        String authCode = "valid-code";
        long mailId = 1L;
        PrintLog printLog = mock(PrintLog.class);
        when(printLog.getMailId()).thenReturn(mailId);
        when(printLog.getPrintDate()).thenReturn(LocalDateTime.now());
        when(printLog.getUsername()).thenReturn("PosId:123");

        when(printLogRepository.findByAuthCode(authCode)).thenReturn(Optional.of(printLog));

        Mail mail = mock(Mail.class);
        when(mail.getMailNumber()).thenReturn("MAIL-001");
        when(mailRepository.findById(mailId)).thenReturn(Optional.of(mail));

        when(encoder.encode(Mail.class, mailId)).thenReturn("encoded-id");

        // Act
        MailSignatureVerificationResponse response = service.verifySignature(authCode, "192.168.1.1");

        // Assert
        assertTrue(response.valid());
        assertEquals("MAIL-001", response.mailNumber());
        assertEquals("encoded-id", response.mailId());
        assertEquals("123", response.signerPosition());
    }
}
