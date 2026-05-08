package id.perumdamts.mail.service.core.folder;

import id.perumdamts.mail.entity.core.MailFolder;
import id.perumdamts.mail.enums.SystemFolder;
import id.perumdamts.mail.repository.core.jpa.MailFolderRepository;
import id.perumdamts.mail.repository.core.jpa.UserTaskRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonalFolderValidatorTest {

    @Mock
    private MailFolderRepository folderRepository;

    @Mock
    private UserTaskRepository userTaskRepository;

    private PersonalFolderValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PersonalFolderValidator(folderRepository, userTaskRepository);
    }

    // ── validateCreate tests ──

    @Test
    void validateCreate_shouldPassForRootLevelFolder() {
        when(folderRepository.findByOwnerIdAndParentFolderIdAndName(1L, null, "My Folder"))
                .thenReturn(Optional.empty());

        validator.validateCreate(1L, SystemFolder.PERSONAL_ROOT.getId(), "My Folder");

        verify(folderRepository).findByOwnerIdAndParentFolderIdAndName(1L, null, "My Folder");
    }

    @Test
    void validateCreate_shouldPassForLevel1Folder() {
        MailFolder parentFolder = new MailFolder(1L, null, "Parent");
        when(folderRepository.findById(11L)).thenReturn(Optional.of(parentFolder));
        when(folderRepository.findByOwnerIdAndParentFolderIdAndName(1L, 11L, "Child"))
                .thenReturn(Optional.empty());

        validator.validateCreate(1L, 11L, "Child");

        verify(folderRepository).findById(11L);
        verify(folderRepository).findByOwnerIdAndParentFolderIdAndName(1L, 11L, "Child");
    }

    @Test
    void validateCreate_shouldFailWhenDepthExceeds3() {
        MailFolder level3 = new MailFolder(1L, 13L, "Level3");
        level3.setId(14L);
        MailFolder level2 = new MailFolder(1L, 12L, "Level2");
        level2.setId(13L);
        MailFolder level1 = new MailFolder(1L, 11L, "Level1");
        level1.setId(12L);

        when(folderRepository.findById(14L)).thenReturn(Optional.of(level3));
        when(folderRepository.findById(13L)).thenReturn(Optional.of(level2));
        when(folderRepository.findById(12L)).thenReturn(Optional.of(level1));
        when(folderRepository.findById(11L)).thenReturn(Optional.of(level1));

        assertThatThrownBy(() -> validator.validateCreate(1L, 14L, "New Folder"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("depth cannot exceed");

        verify(folderRepository).findById(14L);
    }

    @Test
    void validateCreate_shouldFailWhenParentDoesNotExist() {
        when(folderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> validator.validateCreate(1L, 999L, "New Folder"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void validateCreate_shouldFailWhenParentNotOwnedByUser() {
        MailFolder parentFolder = new MailFolder(2L, null, "Other User Folder");
        parentFolder.setId(11L);
        when(folderRepository.findById(11L)).thenReturn(Optional.of(parentFolder));

        assertThatThrownBy(() -> validator.validateCreate(1L, 11L, "New Folder"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not owned by user");
    }

    @Test
    void validateCreate_shouldFailWhenNameDuplicateInSameParent() {
        MailFolder existingFolder = new MailFolder(1L, null, "Existing");
        when(folderRepository.findByOwnerIdAndParentFolderIdAndName(1L, null, "Duplicate"))
                .thenReturn(Optional.of(existingFolder));

        assertThatThrownBy(() -> validator.validateCreate(1L, SystemFolder.PERSONAL_ROOT.getId(), "Duplicate"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void validateCreate_shouldPassForValidDepth2() {
        MailFolder level1 = new MailFolder(1L, null, "Level1");
        level1.setId(11L);
        when(folderRepository.findById(11L)).thenReturn(Optional.of(level1));
        when(folderRepository.findByOwnerIdAndParentFolderIdAndName(1L, 11L, "Level2"))
                .thenReturn(Optional.empty());

        validator.validateCreate(1L, 11L, "Level2");

        verify(folderRepository).findById(11L);
    }

    // ── validateDelete tests ──

    @Test
    void validateDelete_shouldPassForEmptyFolder() {
        MailFolder folder = new MailFolder(1L, null, "Test Folder");
        folder.setId(11L);
        when(folderRepository.findById(11L)).thenReturn(Optional.of(folder));
        when(folderRepository.findActiveChildren(11L)).thenReturn(List.of());
        when(userTaskRepository.countByUserIdAndFolderId(1L, 11L)).thenReturn(0L);

        validator.validateDelete(1L, 11L);

        verify(folderRepository).findById(11L);
        verify(folderRepository).findActiveChildren(11L);
        verify(userTaskRepository).countByUserIdAndFolderId(1L, 11L);
    }

    @Test
    void validateDelete_shouldFailWhenFolderHasChildFolders() {
        MailFolder folder = new MailFolder(1L, null, "Test Folder");
        folder.setId(11L);
        MailFolder child = new MailFolder(1L, 11L, "Child");
        when(folderRepository.findById(11L)).thenReturn(Optional.of(folder));
        when(folderRepository.findActiveChildren(11L)).thenReturn(List.of(child));

        assertThatThrownBy(() -> validator.validateDelete(1L, 11L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("child folders");
    }

    @Test
    void validateDelete_shouldFailWhenFolderHasUserTasks() {
        MailFolder folder = new MailFolder(1L, null, "Test Folder");
        folder.setId(11L);
        when(folderRepository.findById(11L)).thenReturn(Optional.of(folder));
        when(folderRepository.findActiveChildren(11L)).thenReturn(List.of());
        when(userTaskRepository.countByUserIdAndFolderId(1L, 11L)).thenReturn(5L);

        assertThatThrownBy(() -> validator.validateDelete(1L, 11L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non-empty");
    }

    @Test
    void validateDelete_shouldFailWhenFolderNotOwnedByUser() {
        MailFolder folder = new MailFolder(2L, null, "Other User Folder");
        folder.setId(11L);
        when(folderRepository.findById(11L)).thenReturn(Optional.of(folder));

        assertThatThrownBy(() -> validator.validateDelete(1L, 11L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not owned by user");
    }

    @Test
    void validateDelete_shouldFailWhenFolderNotFound() {
        when(folderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> validator.validateDelete(1L, 999L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}