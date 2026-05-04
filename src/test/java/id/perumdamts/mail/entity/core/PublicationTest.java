package id.perumdamts.mail.entity.core;

import id.perumdamts.mail.enums.PublicationStatus;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class PublicationTest {

    @Test
    void onCreate_setsCreatedAtAndUpdatedAt() {
        Publication publication = new Publication();
        publication.onCreate();
        
        assertNotNull(publication.getCreatedAt());
        assertNotNull(publication.getUpdatedAt());
        assertEquals(publication.getCreatedAt(), publication.getUpdatedAt());
    }

    @Test
    void onUpdate_updatesUpdatedAt() throws InterruptedException {
        Publication publication = new Publication();
        publication.onCreate();
        LocalDateTime firstUpdate = publication.getUpdatedAt();
        
        Thread.sleep(10);
        publication.onUpdate();
        
        assertNotNull(publication.getUpdatedAt());
        assertTrue(publication.getUpdatedAt().isAfter(firstUpdate));
    }
    
    @Test
    void publish_setsPublishedDateAndStatus() {
        Publication publication = new Publication();
        publication.publish();
        
        assertEquals(PublicationStatus.PUBLISHED, publication.getStatus());
        assertNotNull(publication.getPublishedDate());
        assertEquals(1, publication.getNotifFlag());
    }

    @Test
    void softDelete_setsStatus() {
        Publication publication = new Publication();
        publication.softDelete();
        
        assertEquals(PublicationStatus.DELETED, publication.getStatus());
    }
}
