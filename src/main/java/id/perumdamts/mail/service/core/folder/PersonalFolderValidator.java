package id.perumdamts.mail.service.core.folder;

import id.perumdamts.mail.entity.core.MailFolder;
import id.perumdamts.mail.enums.SystemFolder;
import id.perumdamts.mail.repository.core.jpa.MailFolderRepository;
import id.perumdamts.mail.repository.core.jpa.UserTaskRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class PersonalFolderValidator {

    private static final int MAX_DEPTH = 3;

    private final MailFolderRepository folderRepository;
    private final UserTaskRepository userTaskRepository;

    public PersonalFolderValidator(MailFolderRepository folderRepository,
            UserTaskRepository userTaskRepository) {
        this.folderRepository = folderRepository;
        this.userTaskRepository = userTaskRepository;
    }

    public void validateCreate(Long userId, Long parentFolderId, String name) {
        validateParentOwnedByUser(userId, parentFolderId);
        validateDepth(userId, parentFolderId);
        validateUniqueNamePerParent(userId, parentFolderId, name);
    }

    public void validateDelete(Long userId, Long folderId) {
        MailFolder folder = getOwnedPersonalFolder(userId, folderId);
        validateFolderEmpty(folder);
    }

    private void validateParentOwnedByUser(Long userId, Long parentFolderId) {
        if (parentFolderId == null || parentFolderId.equals(SystemFolder.PERSONAL_ROOT.getId())) {
            return;
        }
        if (SystemFolder.isPersonalFolder(parentFolderId)) {
            getOwnedPersonalFolder(userId, parentFolderId);
            return;
        }
        throw new IllegalArgumentException("Invalid parent folder: " + parentFolderId);
    }

    private void validateDepth(Long userId, Long parentFolderId) {
        int depth = calculateDepth(userId, parentFolderId);
        if (depth >= MAX_DEPTH) {
            throw new IllegalArgumentException("Folder depth cannot exceed " + MAX_DEPTH + " levels");
        }
    }

    private int calculateDepth(Long userId, Long folderId) {
        if (folderId == null || folderId.equals(SystemFolder.PERSONAL_ROOT.getId())) {
            return 0;
        }
        int depth = 0;
        Long currentId = folderId;
        Set<Long> visited = new HashSet<>();

        while (currentId != null && SystemFolder.isPersonalFolder(currentId)) {
            if (visited.contains(currentId)) {
                throw new IllegalStateException("Circular folder reference detected");
            }
            visited.add(currentId);
            depth++;
            MailFolder folder = folderRepository.findById(currentId)
                    .orElseThrow(() -> new EntityNotFoundException("Folder not found: " + folderId));
            currentId = folder.getParentFolderId();
        }

        return depth;
    }

    private void validateUniqueNamePerParent(Long userId, Long parentFolderId, String name) {
        Long effectiveParentId = (parentFolderId == null || parentFolderId.equals(SystemFolder.PERSONAL_ROOT.getId()))
                ? null
                : parentFolderId;

        boolean exists = folderRepository.findByOwnerIdAndParentFolderIdAndName(userId, effectiveParentId, name.trim())
                .isPresent();

        if (exists) {
            throw new IllegalArgumentException("Folder with name '" + name + "' already exists in this parent");
        }
    }

    private void validateFolderEmpty(MailFolder folder) {
        List<MailFolder> children = folderRepository.findActiveChildren(folder.getId());
        if (!children.isEmpty()) {
            throw new IllegalArgumentException("Cannot delete folder with child folders");
        }

        long mailCount = userTaskRepository.countByUserIdAndFolderId(folder.getOwnerId(), folder.getId());
        if (mailCount > 0) {
            throw new IllegalArgumentException("Cannot delete non-empty folder");
        }
    }

    private MailFolder getOwnedPersonalFolder(Long userId, Long folderId) {
        var folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new EntityNotFoundException("Folder not found: " + folderId));
        if (!folder.isOwnedBy(userId)) {
            throw new IllegalStateException("Folder " + folderId + " is not owned by user " + userId);
        }
        return folder;
    }
}