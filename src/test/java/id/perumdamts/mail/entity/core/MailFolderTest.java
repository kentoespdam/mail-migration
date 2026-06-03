package id.perumdamts.mail.entity.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MailFolderTest {

    @Test
    void constructor_withZeroParent_setsNullParent() {
        MailFolder folder = new MailFolder(100L, 0L, "Root Folder");
        assertNull(folder.getParentFolderId());
    }

    @Test
    void constructor_withNullParent_setsNullParent() {
        MailFolder folder = new MailFolder(100L, null, "Root Folder");
        assertNull(folder.getParentFolderId());
    }

    @Test
    void constructor_withPositiveParent_setsParent() {
        MailFolder folder = new MailFolder(100L, 10L, "Child Folder");
        assertEquals(10L, folder.getParentFolderId());
    }

    @Test
    void softDelete_setsStatusToDeleted() {
        MailFolder folder = new MailFolder(100L, null, "Test");
        folder.softDelete();
        
        assertEquals(3, folder.getStatus());
        assertTrue(folder.isDeleted());
    }
}
