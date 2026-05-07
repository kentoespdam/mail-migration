package id.perumdamts.mail.controller.core;

import id.perumdamts.mail.dto.core.mail.MailSignatureVerificationResponse;
import id.perumdamts.mail.entity.core.Mail;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.service.core.mail.MailSignatureService;
import id.perumdamts.mail.util.SqidsEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailSignatureControllerTest {

    @Mock
    private MailSignatureService signatureService;

    @Mock
    private SqidsEncoder encoder;

    private MailSignatureController controller;

    private MailPrincipal principal;

    @BeforeEach
    void setUp() {
        controller = new MailSignatureController(signatureService, encoder);
        principal = new MailPrincipal("1", "Test User", "test@mail.com",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void signMail_shouldReturnAuthCode() {
        String authCode = "generated-code";
        when(encoder.decode(Mail.class, "encoded-id")).thenReturn(1L);
        when(signatureService.signMail(1L, principal)).thenReturn(authCode);

        String result = controller.signMail(principal, "encoded-id");

        assertThat(result).isEqualTo(authCode);
        verify(signatureService).signMail(1L, principal);
    }

    @Test
    void verifySignature_shouldReturnResponse() {
        String authCode = "valid-code";
        MailSignatureVerificationResponse response = MailSignatureVerificationResponse.valid(
                "1", "001", "Subject", null, "User", "127.0.0.1");
        when(signatureService.verifySignature(authCode)).thenReturn(response);

        MailSignatureVerificationResponse result = controller.verifySignature(authCode);

        assertThat(result).isEqualTo(response);
        verify(signatureService).verifySignature(authCode);
    }
}
