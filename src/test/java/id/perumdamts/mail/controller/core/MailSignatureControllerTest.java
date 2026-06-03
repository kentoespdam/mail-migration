package id.perumdamts.mail.controller.core;

import id.perumdamts.mail.dto.core.mail.MailSignRequest;
import id.perumdamts.mail.dto.core.mail.MailSignResponse;
import id.perumdamts.mail.dto.core.mail.MailSignatureVerificationResponse;
import id.perumdamts.mail.dto.id.MailId;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.service.core.mail.MailSignatureService;
import id.perumdamts.mail.service.security.RateLimitService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailSignatureControllerTest {

    @Mock
    private MailSignatureService signatureService;

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Bucket bucket;

    private MailSignatureController controller;

    private MailPrincipal principal;

    @BeforeEach
    void setUp() {
        controller = new MailSignatureController(signatureService, rateLimitService);
        principal = MailPrincipal.from("1", "Test User", "test@mail.com",
                List.of(new SimpleGrantedAuthority("SCOPE_mail:write")));
    }

    @Test
    void signMail_shouldReturnAuthCodeAndQrUrl() {
        MailSignResponse response = new MailSignResponse("generated-code", "http://localhost:8081/api/mails/verify-sign/generated-code");
        when(signatureService.signMail(1L, 123L)).thenReturn(response);

        MailSignRequest requestBody = new MailSignRequest(123L);
        MailSignResponse result = controller.signMail(principal, new MailId(1L), requestBody);

        assertThat(result.authCode()).isEqualTo("generated-code");
        assertThat(result.qrUrl()).contains("verify-sign");
        verify(signatureService).signMail(1L, 123L);
    }

    @Test
    void verifySignature_shouldReturnResponse() {
        String authCode = "valid-code";
        MailSignatureVerificationResponse response = MailSignatureVerificationResponse.valid(
                "1", "001", null, "User", "123", "ARCHIVED");

        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(rateLimitService.resolveBucket("127.0.0.1")).thenReturn(bucket);
        when(bucket.tryConsume(1)).thenReturn(true);
        when(signatureService.verifySignature(authCode, "127.0.0.1")).thenReturn(response);

        MailSignatureVerificationResponse result = controller.verifySignature(authCode, request);

        assertThat(result).isEqualTo(response);
        verify(signatureService).verifySignature(authCode, "127.0.0.1");
    }
}
