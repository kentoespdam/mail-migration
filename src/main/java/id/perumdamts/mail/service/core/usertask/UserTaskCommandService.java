package id.perumdamts.mail.service.core.usertask;

import id.perumdamts.mail.entity.core.UserTask;
import id.perumdamts.mail.enums.SystemFolder;
import id.perumdamts.mail.repository.core.jpa.UserTaskRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserTaskCommandService {
    private final UserTaskRepository userTaskRepository;

    @Transactional
    public void createDraft(Long userId, Long mailId) {
        userTaskRepository.save(UserTask.draft(userId, mailId));
    }

    @Transactional
    public void createInboxes(Long mailId, List<Long> recipientIds) {
        List<UserTask> inboxTasks = recipientIds.stream()
                .map(userId -> UserTask.inbox(userId, mailId))
                .toList();
        userTaskRepository.saveAll(inboxTasks);
    }

    @Transactional
    public void moveFromDraftToSent(Long userId, Long mailId) {
        userTaskRepository.updateFolder(userId, mailId,
                SystemFolder.DRAFT.getId(), SystemFolder.SENT.getId());
    }

    @Transactional
    public void markParentAsRead(Long userId, Long parentMailId) {
        userTaskRepository.updateFolder(userId, parentMailId,
                SystemFolder.INBOX.getId(), SystemFolder.READ.getId());
    }

    @Transactional
    public void softDelete(Long userId, Long mailId) {
        UserTask userTask = getUserTaskOrThrow(userId, mailId);
        userTask.softDelete();
        userTaskRepository.save(userTask);
    }

    @Transactional
    public void purge(Long userId, Long mailId) {
        UserTask userTask = getUserTaskOrThrow(userId, mailId);
        userTask.purge();
        userTaskRepository.save(userTask);
    }

    @Transactional
    public void restore(Long userId, Long mailId) {
        UserTask userTask = getUserTaskOrThrow(userId, mailId);
        if (!userTask.isInTrash()) {
            throw new IllegalStateException("Mail is not in trash");
        }
        userTask.restore();
        userTaskRepository.save(userTask);
    }

    @Transactional
    public void markRead(Long userId, Long mailId) {
        UserTask userTask = getUserTaskOrThrow(userId, mailId);
        userTask.markRead();
        userTaskRepository.save(userTask);
    }

    @Transactional
    public void relocateMails(Long userId, Long sourceFolderId, Long targetFolderId) {
        userTaskRepository.relocateMails(userId, sourceFolderId, targetFolderId);
    }

    @Transactional
    public void updateFolder(Long userId, Long mailId, Long fromFolderId, Long toFolderId) {
        userTaskRepository.updateFolder(userId, mailId, fromFolderId, toFolderId);
    }

    @Transactional
    public void purgeTrash(Long userId) {
        userTaskRepository.purgeTrash(userId);
    }

    private UserTask getUserTaskOrThrow(Long userId, Long mailId) {
        return userTaskRepository.findByUserIdAndMailIdAnyFolder(userId, mailId)
                .orElseThrow(() -> new EntityNotFoundException("UserTask not found for user " + userId + " and mail " + mailId));
    }
}
