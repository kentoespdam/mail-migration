package id.perumdamts.mail.entity.master;

import id.perumdamts.mail.enums.CategoryStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class MailCategoryTest {

    @Test
    void constructor_setsFieldsCorrectly() {
        MailType mailType = mock(MailType.class);
        MailCategory category = new MailCategory(mailType, "CODE", "Name");
        
        assertEquals(mailType, category.getMailType());
        assertEquals("CODE", category.getCode());
        assertEquals("Name", category.getName());
        assertEquals(CategoryStatus.ENABLED, category.getStatus());
    }

    @Test
    void markDeleted_setsStatus() {
        MailCategory category = new MailCategory(null, "C", "N");
        category.markDeleted();
        assertEquals(CategoryStatus.DELETED, category.getStatus());
    }

    @Test
    void toggleStatus_works() {
        MailCategory category = new MailCategory(null, "C", "N");
        assertTrue(category.isActive());
        
        category.disable();
        assertFalse(category.isActive());
        assertEquals(CategoryStatus.DISABLED, category.getStatus());
        
        category.enable();
        assertTrue(category.isActive());
        assertEquals(CategoryStatus.ENABLED, category.getStatus());
    }
}
