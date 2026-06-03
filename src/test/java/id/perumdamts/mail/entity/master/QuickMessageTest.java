package id.perumdamts.mail.entity.master;

import id.perumdamts.mail.enums.RecordStatusActive;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class QuickMessageTest {

    @Test
    void onCreate_setsDates() {
        QuickMessage msg = new QuickMessage("Hello");
        msg.onCreate();
        
        assertNotNull(msg.getCreatedDate());
        assertNotNull(msg.getUpdatedDate());
        assertEquals(msg.getCreatedDate(), msg.getUpdatedDate());
    }

    @Test
    void onUpdate_updatesDate() throws InterruptedException {
        QuickMessage msg = new QuickMessage("Hello");
        msg.onCreate();
        LocalDateTime first = msg.getUpdatedDate();
        
        Thread.sleep(10);
        msg.onUpdate();
        
        assertTrue(msg.getUpdatedDate().isAfter(first));
    }

    @Test
    void markDeleted_setsFlag() {
        QuickMessage msg = new QuickMessage("Hello");
        msg.markDeleted();
        
        assertTrue(msg.getDeleted());
    }

    @Test
    void toggleStatus_works() {
        QuickMessage msg = new QuickMessage("Hello");
        assertEquals(RecordStatusActive.ACTIVE, msg.getStatus());
        
        msg.toggleStatus();
        assertEquals(RecordStatusActive.INACTIVE, msg.getStatus());
    }
    
    @Test
    void isActive_returnsCorrectStatus() {
        QuickMessage msg = new QuickMessage("Hello");
        assertTrue(msg.isActive());
        
        msg.toggleStatus();
        assertFalse(msg.isActive());
        
        msg.toggleStatus();
        msg.markDeleted();
        assertFalse(msg.isActive());
    }
}
