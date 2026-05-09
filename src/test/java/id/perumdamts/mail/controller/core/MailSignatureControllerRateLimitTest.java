package id.perumdamts.mail.controller.core;

import id.perumdamts.mail.dto.core.mail.MailSignatureVerificationResponse;
import id.perumdamts.mail.service.core.mail.MailSignatureService;
import id.perumdamts.mail.service.security.RateLimitService;
import id.perumdamts.mail.util.SqidsEncoder;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class MailSignatureControllerRateLimitTest {

    private MockMvc mockMvc;

    @Mock
    private MailSignatureService signatureService;

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private SqidsEncoder encoder;

    @Mock
    private HttpServletRequest request;

    private final RateLimitService actualRateLimitService = new RateLimitService();

    @BeforeEach
    public void setup() {
        MailSignatureController controller = new MailSignatureController(signatureService, actualRateLimitService, encoder);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void testRateLimit() throws Exception {
        String authCode = "someCode";
        when(signatureService.verifySignature(anyString(), anyString())).thenReturn(MailSignatureVerificationResponse.invalid("invalid"));

        // Hit 30 times
        for (int i = 0; i < 30; i++) {
            mockMvc.perform(get("/api/mails/verify-sign/" + authCode)
                    .with(req -> {
                        req.setRemoteAddr("127.0.0.1");
                        return req;
                    }))
                    .andExpect(status().isOk());
        }

        // 31st hit should still be OK (200) but with invalid response as per PRD "always 200"
        mockMvc.perform(get("/api/mails/verify-sign/" + authCode)
                .with(req -> {
                    req.setRemoteAddr("127.0.0.1");
                    return req;
                }))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String content = result.getResponse().getContentAsString();
                    assertThat(content).contains("\"valid\":false");
                    assertThat(content).contains("Too many requests");
                });
        
        // Different IP should still be OK
        mockMvc.perform(get("/api/mails/verify-sign/" + authCode)
                .with(req -> {
                    req.setRemoteAddr("127.0.0.2");
                    return req;
                }))
                .andExpect(status().isOk());
    }
}
