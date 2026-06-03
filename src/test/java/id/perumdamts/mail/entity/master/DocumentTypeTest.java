package id.perumdamts.mail.entity.master;

import id.perumdamts.mail.enums.RecordStatusActive;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DocumentTypeTest {

    @Test
    void markDeleted_setsDeletedToTrue() {
        DocumentType documentType = new DocumentType("Test");
        documentType.markDeleted();
        
        assertTrue(documentType.getDeleted());
    }

    @Test
    void toggleStatus_switchesStatus() {
        DocumentType documentType = new DocumentType("Test");
        assertEquals(RecordStatusActive.ACTIVE, documentType.getStatus());
        
        documentType.toggleStatus();
        assertEquals(RecordStatusActive.INACTIVE, documentType.getStatus());
        
        documentType.toggleStatus();
        assertEquals(RecordStatusActive.ACTIVE, documentType.getStatus());
    }

    @Test
    void toggleStatus_onDeleted_throwsException() {
        DocumentType documentType = new DocumentType("Test");
        documentType.markDeleted();
        
        assertThrows(IllegalStateException.class, documentType::toggleStatus);
    }

    @Test
    void isActive_returnsTrueOnlyWhenActiveAndNotDeleted() {
        DocumentType documentType = new DocumentType("Test");
        assertTrue(documentType.isActive());
        
        documentType.toggleStatus();
        assertFalse(documentType.isActive());
        
        documentType.toggleStatus();
        documentType.markDeleted();
        assertFalse(documentType.isActive());
    }
}
