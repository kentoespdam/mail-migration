package id.perumdamts.mail.entity.core;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class PrintLogTest {

    @Test
    void create_setsFieldsCorrectly() {
        Long mailId = 1L;
        String username = "testuser";
        String ipAddress = "127.0.0.1";
        
        PrintLog log = PrintLog.create(mailId, username, ipAddress);
        
        assertEquals(mailId, log.getMailId());
        assertEquals(username, log.getUsername());
        assertEquals(ipAddress, log.getIpAddress());
        assertNotNull(log.getPrintDate());
    }

    @Test
    void getVerificationUrl_returnsCorrectUrl() {
        PrintLog log = new PrintLog();
        log.setAuthCode("ABC123XYZ");
        
        String url = log.getVerificationUrl("http://localhost:8080");
        assertEquals("http://localhost:8080/api/mails/verify-sign/ABC123XYZ", url);
    }
}
