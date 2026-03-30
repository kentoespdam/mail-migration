package id.perumdamts.mail.service.core.folder;

import id.perumdamts.mail.config.CacheConfig;
import id.perumdamts.mail.dto.core.folder.*;
import id.perumdamts.mail.dto.core.mail.MailSummaryResponse;
import id.perumdamts.mail.entity.core.PersonalFolder;
import id.perumdamts.mail.entity.core.UserTask;
import id.perumdamts.mail.enums.SystemFolder;
import id.perumdamts.mail.repository.core.jooq.FolderCounterRepository;
import id.perumdamts.mail.repository.core.jooq.MailQueryRepository;
import id.perumdamts.mail.repository.core.jpa.MailFolderRepository;
import id.perumdamts.mail.repository.core.jpa.UserTaskRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Service untuk operasi MailFolder.
 * - System folders dari enum SystemFolder
 * - Personal folders dari DB (entity PersonalFolder)
 * - Counter badge dari JOOQ window function
 * - Soft delete 2-level: Trash (folder 6) → Purge (hapus permanent)
 */
@Service
@Transactional
public class MailFolderService {

    private final MailFolderRepository folderRepository;
    private final UserTaskRepository userTaskRepository;
    private final FolderCounterRepository counterRepository;
    private final MailQueryRepository mailQueryRepository;
    private final MailFolderMapper folderMapper;

    public MailFolderService(MailFolderRepository folderRepository,
                              UserTaskRepository userTaskRepository,
                              FolderCounterRepository counterRepository,
                              MailQueryRepository mailQueryRepository,
                              MailFolderMapper folderMapper) {
        this.folderRepository = folderRepository;
        this.userTaskRepository = userTaskRepository;
        this.counterRepository = counterRepository;
        this.mailQueryRepository = mailQueryRepository;
        this.folderMapper = folderMapper;
    }

    // ── Query Operations ──

    /**
     * Get folder tree untuk user: system folders + personal folders.
     * Inject counter (unread/total) dari JOOQ query.
     */
    @Cacheable(value = CacheConfig.CacheNames.MAIL_FOLDER, key = "'tree:' + #userId")
    public List<MailFolderResponse> getFolderTree(Integer userId) {
        // Get counters map dari JOOQ (1 query, bukan N+1)
        Map<Integer, FolderCountDto> countersMap = counterRepository.getCountersMap(userId);

        List<MailFolderResponse> tree = new ArrayList<>();

        // System folders dari enum
        Arrays.stream(SystemFolder.values())
                .filter(sf -> sf != SystemFolder.PURGED)
                .map(sf -> {
                    FolderCountDto counter = countersMap.get(sf.getId());
                    return fromSystemFolder(sf, counter);
                })
                .forEach(tree::add);

        // Personal folders dari DB
        folderRepository.findByOwnerIdOrderByParentFolderIdAscIdAsc(userId).stream()
                .map(folder -> {
                    FolderCountDto counter = countersMap.get(folder.getId());
                    return toResponseWithCounter(folder, counter);
                })
                .forEach(tree::add);

        return tree;
    }

    /**
     * Get counter badge per folder (endpoint terpisah).
     */
    public List<FolderCounterResponse> getCounters(Integer userId) {
        Map<Integer, FolderCountDto> countersMap = counterRepository.getCountersMap(userId);
        return countersMap.values().stream()
                .map(c -> new FolderCounterResponse(c.folderId(), c.folderName(), c.unread(), c.total()))
                .toList();
    }

    public List<MailSummaryResponse> getMailsInFolder(Integer userId, Integer folderId,
                                                       int page, int size,
                                                       String sortBy, String sortDir,
                                                       String keyword) {
        validateFolderAccess(userId, folderId);
        return mailQueryRepository.findMailsInFolder(userId, folderId, page * size, size, sortBy, sortDir, keyword);
    }

    // ── Command Operations — Personal Folder CRUD ──

    /**
     * Create personal folder baru.
     */
    @CacheEvict(value = CacheConfig.CacheNames.MAIL_FOLDER, key = "'tree:' + #userId")
    public MailFolderResponse createFolder(Integer userId, MailFolderRequest request) {
        validateParentFolder(userId, request.parentFolderId());

        if (folderRepository.existsByOwnerIdAndName(userId, request.name().trim())) {
            throw new IllegalArgumentException("Folder with name '" + request.name() + "' already exists");
        }

        var folder = new PersonalFolder(userId, request.parentFolderId(), request.name().trim());
        return folderMapper.toResponse(folderRepository.save(folder));
    }

    /**
     * Rename personal folder.
     */
    @CacheEvict(value = CacheConfig.CacheNames.MAIL_FOLDER, key = "'tree:' + #userId")
    public MailFolderResponse renameFolder(Integer userId, Integer folderId, MailFolderRequest request) {
        PersonalFolder folder = getOwnedPersonalFolder(userId, folderId);

        if (folderRepository.existsByOwnerIdAndNameAndIdNot(userId, request.name().trim(), folderId)) {
            throw new IllegalArgumentException("Folder with name '" + request.name() + "' already exists");
        }

        folder.rename(request.name());
        return folderMapper.toResponse(folderRepository.save(folder));
    }

    /**
     * Delete personal folder (soft delete).
     * Mails di-relocate ke parent folder.
     */
    @CacheEvict(value = CacheConfig.CacheNames.MAIL_FOLDER, key = "'tree:' + #userId")
    public void deleteFolder(Integer userId, Integer folderId) {
        PersonalFolder folder = getOwnedPersonalFolder(userId, folderId);

        // Relocate mails ke parent folder
        userTaskRepository.relocateMails(userId, folderId, folder.getParentFolderId());

        // Cascade soft-delete children
        var children = folderRepository.findActiveChildren(folderId);
        for (var child : children) {
            userTaskRepository.relocateMails(userId, child.getId(), folder.getParentFolderId());
            child.softDelete();
        }

        folder.softDelete();
        folderRepository.save(folder);
    }

    // ── Mail Operations ──

    /**
     * Move mails dari satu folder ke folder lain.
     */
    public void moveMails(Integer userId, MoveMailRequest request) {
        validateTargetFolder(userId, request.toFolderId());
        for (Integer mailId : request.mailIds()) {
            userTaskRepository.updateFolder(userId, mailId, request.fromFolderId(), request.toFolderId());
        }
    }

    /**
     * Delete mail — soft delete 2-level:
     * - Jika belum di trash → pindah ke DELETED(6)
     * - Jika sudah di trash → purge (hapus permanent, folder_id=-1)
     */
    public void deleteMail(Integer userId, Integer mailId) {
        UserTask userTask = userTaskRepository.findByUserIdAndMailIdAnyFolder(userId, mailId)
                .orElseThrow(() -> new EntityNotFoundException("Mail not found: " + mailId));

        if (userTask.getFolderId() == SystemFolder.DELETED.getId()) {
            // Sudah di trash → purge
            userTask.purge();
        } else {
            // Belum di trash → soft delete ke DELETED
            userTask.softDelete();
        }
        userTaskRepository.save(userTask);
    }

    /**
     * Restore mail dari trash ke folder asal.
     * restore_folder_id diambil dari DB (bukan dari client) — fix issue B4.
     */
    public void restoreMail(Integer userId, Integer mailId) {
        UserTask userTask = userTaskRepository.findByUserIdAndMailId(userId, mailId)
                .orElseThrow(() -> new EntityNotFoundException("Mail not found: " + mailId));

        if (userTask.getFolderId() != SystemFolder.DELETED.getId()) {
            throw new IllegalStateException("Mail is not in trash");
        }

        if (userTask.getRestoreFolderId() == null) {
            throw new IllegalStateException("No restore folder recorded for this mail");
        }

        // Restore ke folder asal (dari DB)
        userTask.restore();
        userTaskRepository.save(userTask);
    }

    /**
     * Empty trash — purge semua mail di folder DELETED.
     * @return jumlah mail yang di-purge
     */
    public int emptyTrash(Integer userId) {
        return userTaskRepository.purgeTrash(userId);
    }

    // ── Private Helpers ──

    private MailFolderResponse fromSystemFolder(SystemFolder sf, FolderCountDto counter) {
        Integer parentFolderId = sf.getParent() != null ? sf.getParent().getId() : 0;
        return new MailFolderResponse(
                sf.getId(),
                parentFolderId,
                0,
                sf.getDisplayName(),
                "email",
                true,
                counter != null ? counter.unread() : 0L,
                counter != null ? counter.total() : 0L
        );
    }

    private MailFolderResponse toResponseWithCounter(PersonalFolder folder, FolderCountDto counter) {
        return new MailFolderResponse(
                folder.getId(),
                folder.getParentFolderId(),
                folder.getOwnerId(),
                folder.getName(),
                folder.getIconClsFolder(),
                false,
                counter != null ? counter.unread() : 0L,
                counter != null ? counter.total() : 0L
        );
    }

    private PersonalFolder getOwnedPersonalFolder(Integer userId, Integer folderId) {
        var folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new EntityNotFoundException("Folder not found: " + folderId));
        if (!folder.isOwnedBy(userId)) {
            throw new IllegalStateException("Folder " + folderId + " is not owned by user " + userId);
        }
        return folder;
    }

    private void validateFolderAccess(Integer userId, Integer folderId) {
        // System folders — valid jika ada di enum dan bukan PURGED
        if (SystemFolder.findById(folderId).filter(sf -> sf != SystemFolder.PURGED).isPresent()) {
            return;
        }
        // Personal folders — harus owned by user
        if (SystemFolder.isPersonalFolder(folderId)) {
            getOwnedPersonalFolder(userId, folderId);
            return;
        }
        throw new IllegalArgumentException("Invalid folder: " + folderId);
    }

    private void validateParentFolder(Integer userId, Integer parentFolderId) {
        if (parentFolderId == SystemFolder.PERSONAL_ROOT.getId()) return;
        if (SystemFolder.isPersonalFolder(parentFolderId)) {
            getOwnedPersonalFolder(userId, parentFolderId);
            return;
        }
        throw new IllegalArgumentException("Invalid parent folder: " + parentFolderId);
    }

    private void validateTargetFolder(Integer userId, Integer targetFolderId) {
        // System folders — hanya yang movable (READ, DELETED)
        SystemFolder.findById(targetFolderId).ifPresent(sf -> {
            if (!sf.isMovable()) {
                throw new IllegalArgumentException("Invalid move target folder: " + targetFolderId);
            }
        });
        if (SystemFolder.findById(targetFolderId).isPresent()) return;

        // Personal folders yang owned by user
        if (SystemFolder.isPersonalFolder(targetFolderId)) {
            getOwnedPersonalFolder(userId, targetFolderId);
            return;
        }
        throw new IllegalArgumentException("Invalid move target folder: " + targetFolderId);
    }
}
