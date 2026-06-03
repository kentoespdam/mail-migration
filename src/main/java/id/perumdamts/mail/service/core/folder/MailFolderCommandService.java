package id.perumdamts.mail.service.core.folder;

import id.perumdamts.mail.config.CacheConfig;
import id.perumdamts.mail.dto.core.folder.MailFolderMapper;
import id.perumdamts.mail.dto.core.folder.MailFolderRequest;
import id.perumdamts.mail.dto.core.folder.MailFolderResponse;
import id.perumdamts.mail.dto.core.folder.MoveMailRequest;
import id.perumdamts.mail.entity.core.Mail;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.entity.core.MailFolder;
import id.perumdamts.mail.enums.SystemFolder;
import id.perumdamts.mail.repository.core.jpa.MailFolderRepository;
import id.perumdamts.mail.service.core.usertask.UserTaskCommandService;
import id.perumdamts.mail.service.core.usertask.UserTaskQueryService;
import id.perumdamts.mail.util.SqidsEncoder;
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
    private final SqidsEncoder encoder;
    private final id.perumdamts.mail.service.core.mail.AuditTrailService auditTrailService;

    public MailFolderCommandService(MailFolderRepository folderRepository,
            UserTaskCommandService userTaskCommandService,
            UserTaskQueryService userTaskQueryService,
            MailFolderMapper folderMapper,
            SqidsEncoder encoder,
            id.perumdamts.mail.service.core.mail.AuditTrailService auditTrailService) {
        this.folderRepository = folderRepository;
        this.userTaskCommandService = userTaskCommandService;
        this.userTaskQueryService = userTaskQueryService;
        this.folderMapper = folderMapper;
        this.encoder = encoder;
        this.auditTrailService = auditTrailService;
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

        userTaskCommandService.relocateMails(userId, folderId, folder.getParentFolderId());

        var children = folderRepository.findActiveChildren(folderId);
        for (var child : children) {
            userTaskCommandService.relocateMails(userId, child.getId(), folder.getParentFolderId());
            child.softDelete();
        }

        folder.softDelete();
        folderRepository.save(folder);
    }

    @Transactional
    public void moveMails(MailPrincipal principal, MoveMailRequest request) {
        Long userId = principal.userIdLong();
        Long toFolderId = encoder.decode(MailFolder.class, request.toFolderId());
        Long fromFolderId = encoder.decode(MailFolder.class, request.fromFolderId());
        validateTargetFolder(userId, toFolderId);
        for (String mailSqid : request.mailIds()) {
            Long mailId = encoder.decode(Mail.class, mailSqid);
            userTaskCommandService.updateFolder(userId, mailId, fromFolderId, toFolderId);
            auditTrailService.logAction(mailId, "MOVE", principal.getUsername(), "Memindahkan surat ke folder lain");
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
