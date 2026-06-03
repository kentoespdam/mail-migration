package id.perumdamts.mail.service.core.folder;

import id.perumdamts.mail.config.CacheConfig;
import id.perumdamts.mail.dto.core.folder.MailFolderMapper;
import id.perumdamts.mail.dto.core.folder.MailFolderRequest;
import id.perumdamts.mail.dto.core.folder.MailFolderResponse;
import id.perumdamts.mail.dto.core.folder.MoveMailRequest;
import id.perumdamts.mail.dto.id.MailId;
import id.perumdamts.mail.entity.core.MailFolder;
import id.perumdamts.mail.enums.SystemFolder;
import id.perumdamts.mail.repository.core.jpa.MailFolderRepository;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.service.core.usertask.UserTaskCommandService;
import id.perumdamts.mail.service.core.usertask.UserTaskQueryService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MailFolderCommandService {

    private final MailFolderRepository folderRepository;
    private final UserTaskCommandService userTaskCommandService;
    private final UserTaskQueryService userTaskQueryService;
    private final MailFolderMapper folderMapper;
    private final id.perumdamts.mail.service.core.mail.AuditTrailService auditTrailService;
    private final PersonalFolderValidator personalFolderValidator;

    public MailFolderCommandService(MailFolderRepository folderRepository,
            UserTaskCommandService userTaskCommandService,
            UserTaskQueryService userTaskQueryService,
            MailFolderMapper folderMapper,
            id.perumdamts.mail.service.core.mail.AuditTrailService auditTrailService,
            PersonalFolderValidator personalFolderValidator) {
        this.folderRepository = folderRepository;
        this.userTaskCommandService = userTaskCommandService;
        this.userTaskQueryService = userTaskQueryService;
        this.folderMapper = folderMapper;
        this.auditTrailService = auditTrailService;
        this.personalFolderValidator = personalFolderValidator;
    }

    @Transactional
    @CacheEvict(value = CacheConfig.CacheNames.MAIL_FOLDER, key = "'tree:' + #userId")
    public MailFolderResponse createFolder(Long userId, MailFolderRequest request) {
        Long parentFolderId = request.parentFolderId() != null
                ? request.parentFolderId().value()
                : SystemFolder.PERSONAL_ROOT.getId();
        personalFolderValidator.validateCreate(userId, parentFolderId, request.name());

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
        personalFolderValidator.validateDelete(userId, folderId);

        MailFolder folder = getOwnedPersonalFolder(userId, folderId);
        folder.softDelete();
        folderRepository.save(folder);
    }

    @Transactional
    public void moveMails(MailPrincipal principal, MoveMailRequest request) {
        Long userId = principal.userIdLong();
        Long toFolderId = request.toFolderId().value();
        Long fromFolderId = request.fromFolderId().value();
        validateTargetFolder(userId, toFolderId);
        for (MailId mailId : request.mailIds()) {
            userTaskCommandService.updateFolder(userId, mailId.value(), fromFolderId, toFolderId);
            auditTrailService.logAction(mailId.value(), "MOVE", principal.getUsername(), "Memindahkan surat ke folder lain");
        }
    }

    @Transactional
    public void deleteMail(MailPrincipal principal, Long mailId) {
        Long userId = principal.userIdLong();
        userTaskQueryService.findUserTask(userId, mailId)
                .ifPresentOrElse(
                        ut -> {
                            if (ut.isInTrash()) {
                                userTaskCommandService.purge(userId, mailId);
                                auditTrailService.logAction(mailId, "PURGE", principal.getUsername(), "Menghapus surat secara permanen");
                            } else {
                                userTaskCommandService.softDelete(userId, mailId);
                                auditTrailService.logAction(mailId, "DELETE", principal.getUsername(), "Memindahkan surat ke kotak sampah");
                            }
                        },
                        () -> {
                            throw new EntityNotFoundException("Mail not found: " + mailId);
                        });
    }

    @Transactional
    public void restoreMail(MailPrincipal principal, Long mailId) {
        userTaskCommandService.restore(principal.userIdLong(), mailId);
        auditTrailService.logAction(mailId, "RESTORE", principal.getUsername(), "Mengembalikan surat dari kotak sampah");
    }

    @Transactional
    public void emptyTrash(Long userId) {
        userTaskCommandService.purgeTrash(userId);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.CacheNames.MAIL_FOLDER, key = "'tree:' + #userId")
    public void ensureSystemFolders(Long userId) {
        if (folderRepository.existsByOwnerIdAndStatus(userId, 1)) {
            return;
        }
        var personalRoot = new MailFolder(userId, SystemFolder.PERSONAL_ROOT.getId(), "Personal Folder");
        folderRepository.save(personalRoot);
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
