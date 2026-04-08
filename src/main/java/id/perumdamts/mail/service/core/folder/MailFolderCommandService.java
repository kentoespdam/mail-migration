package id.perumdamts.mail.service.core.folder;

import id.perumdamts.mail.config.CacheConfig;
import id.perumdamts.mail.dto.core.folder.MailFolderMapper;
import id.perumdamts.mail.dto.core.folder.MailFolderRequest;
import id.perumdamts.mail.dto.core.folder.MailFolderResponse;
import id.perumdamts.mail.dto.core.folder.MoveMailRequest;
import id.perumdamts.mail.entity.core.Mail;
import id.perumdamts.mail.entity.core.MailFolder;
import id.perumdamts.mail.entity.core.UserTask;
import id.perumdamts.mail.enums.SystemFolder;
import id.perumdamts.mail.repository.core.jpa.MailFolderRepository;
import id.perumdamts.mail.repository.core.jpa.UserTaskRepository;
import id.perumdamts.mail.util.SqidsEncoder;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MailFolderCommandService {

    private final MailFolderRepository folderRepository;
    private final UserTaskRepository userTaskRepository;
    private final MailFolderMapper folderMapper;
    private final SqidsEncoder encoder;

    public MailFolderCommandService(MailFolderRepository folderRepository,
            UserTaskRepository userTaskRepository,
            MailFolderMapper folderMapper,
            SqidsEncoder encoder) {
        this.folderRepository = folderRepository;
        this.userTaskRepository = userTaskRepository;
        this.folderMapper = folderMapper;
        this.encoder = encoder;
    }

    @Transactional
    @CacheEvict(value = CacheConfig.CacheNames.MAIL_FOLDER, key = "'tree:' + #userId")
    public MailFolderResponse createFolder(Long userId, MailFolderRequest request) {
        Long parentFolderId = request.parentFolderId() != null
                ? encoder.decode(MailFolder.class, request.parentFolderId())
                : SystemFolder.PERSONAL_ROOT.getId();
        validateParentFolder(userId, parentFolderId);

        if (folderRepository.existsByOwnerIdAndName(userId, request.name().trim())) {
            throw new IllegalArgumentException("Folder with name '" + request.name() + "' already exists");
        }

        var folder = new MailFolder(userId, parentFolderId, request.name().trim());
        return folderMapper.toResponse(folderRepository.save(folder));
    }

    @Transactional
    @CacheEvict(value = CacheConfig.CacheNames.MAIL_FOLDER, key = "'tree:' + #userId")
    public MailFolderResponse renameFolder(Long userId, Long folderId, MailFolderRequest request) {
        MailFolder folder = getOwnedPersonalFolder(userId, folderId);

        if (folderRepository.existsByOwnerIdAndNameAndIdNot(userId, request.name().trim(), folderId)) {
            throw new IllegalArgumentException("Folder with name '" + request.name() + "' already exists");
        }

        folder.rename(request.name());
        return folderMapper.toResponse(folderRepository.save(folder));
    }

    @Transactional
    @CacheEvict(value = CacheConfig.CacheNames.MAIL_FOLDER, key = "'tree:' + #userId")
    public void deleteFolder(Long userId, Long folderId) {
        MailFolder folder = getOwnedPersonalFolder(userId, folderId);

        userTaskRepository.relocateMails(userId, folderId, folder.getParentFolderId());

        var children = folderRepository.findActiveChildren(folderId);
        for (var child : children) {
            userTaskRepository.relocateMails(userId, child.getId(), folder.getParentFolderId());
            child.softDelete();
        }

        folder.softDelete();
        folderRepository.save(folder);
    }

    @Transactional
    public void moveMails(Long userId, MoveMailRequest request) {
        Long toFolderId = encoder.decode(MailFolder.class, request.toFolderId());
        Long fromFolderId = encoder.decode(MailFolder.class, request.fromFolderId());
        validateTargetFolder(userId, toFolderId);
        for (String mailSqid : request.mailIds()) {
            Long mailId = encoder.decode(Mail.class, mailSqid);
            userTaskRepository.updateFolder(userId, mailId, fromFolderId, toFolderId);
        }
    }

    @Transactional
    public void deleteMail(Long userId, Long mailId) {
        UserTask userTask = userTaskRepository.findByUserIdAndMailIdAnyFolder(userId, mailId)
                .orElseThrow(() -> new EntityNotFoundException("Mail not found: " + mailId));

        if (userTask.getFolderId().equals(SystemFolder.DELETED.getId())) {
            userTask.purge();
        } else {
            userTask.softDelete();
        }
        userTaskRepository.save(userTask);
    }

    @Transactional
    public void restoreMail(Long userId, Long mailId) {
        UserTask userTask = userTaskRepository.findByUserIdAndMailIdAnyFolder(userId, mailId)
                .orElseThrow(() -> new EntityNotFoundException("Mail not found: " + mailId));

        if (!userTask.getFolderId().equals(SystemFolder.DELETED.getId())) {
            throw new IllegalStateException("Mail is not in trash");
        }

        if (userTask.getRestoreFolderId() == null) {
            throw new IllegalStateException("No restore folder recorded for this mail");
        }

        userTask.restore();
        userTaskRepository.save(userTask);
    }

    @Transactional
    public void emptyTrash(Long userId) {
        userTaskRepository.purgeTrash(userId);
    }

    private MailFolder getOwnedPersonalFolder(Long userId, Long folderId) {
        var folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new EntityNotFoundException("Folder not found: " + folderId));
        if (!folder.isOwnedBy(userId)) {
            throw new IllegalStateException("Folder " + folderId + " is not owned by user " + userId);
        }
        return folder;
    }

    private void validateParentFolder(Long userId, Long parentFolderId) {
        if (parentFolderId.equals(SystemFolder.PERSONAL_ROOT.getId()))
            return;
        if (SystemFolder.isPersonalFolder(parentFolderId)) {
            getOwnedPersonalFolder(userId, parentFolderId);
            return;
        }
        throw new IllegalArgumentException("Invalid parent folder: " + parentFolderId);
    }

    private void validateTargetFolder(Long userId, Long targetFolderId) {
        SystemFolder.findById(targetFolderId).ifPresent(sf -> {
            if (!sf.isMovable()) {
                throw new IllegalArgumentException("Invalid move target folder: " + targetFolderId);
            }
        });
        if (SystemFolder.findById(targetFolderId).isPresent())
            return;

        if (SystemFolder.isPersonalFolder(targetFolderId)) {
            getOwnedPersonalFolder(userId, targetFolderId);
            return;
        }
        throw new IllegalArgumentException("Invalid move target folder: " + targetFolderId);
    }
}
