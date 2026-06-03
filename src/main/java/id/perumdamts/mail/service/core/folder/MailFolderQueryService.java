package id.perumdamts.mail.service.core.folder;

import id.perumdamts.mail.config.CacheConfig;
import id.perumdamts.mail.dto.core.folder.*;
import id.perumdamts.mail.dto.core.mail.MailComponentDto;
import id.perumdamts.mail.dto.core.mail.MailSummaryResponse;
import id.perumdamts.mail.dto.id.MailFolderId;
import id.perumdamts.mail.dto.id.UserId;
import id.perumdamts.mail.entity.core.MailFolder;
import id.perumdamts.mail.enums.SystemFolder;
import id.perumdamts.mail.repository.core.jooq.FolderCounterRepository;
import id.perumdamts.mail.repository.core.jooq.MailQueryRepository;
import id.perumdamts.mail.repository.core.jpa.MailFolderRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class MailFolderQueryService {

    private final MailFolderRepository folderRepository;
    private final FolderCounterRepository counterRepository;
    private final MailQueryRepository mailQueryRepository;

    public MailFolderQueryService(MailFolderRepository folderRepository,
            FolderCounterRepository counterRepository,
            MailQueryRepository mailQueryRepository) {
        this.folderRepository = folderRepository;
        this.counterRepository = counterRepository;
        this.mailQueryRepository = mailQueryRepository;
    }

    @Cacheable(value = CacheConfig.CacheNames.MAIL_FOLDER, key = "'tree:' + #userId")
    public List<MailFolderResponse> getFolderTree(Long userId) {
        Map<Long, FolderCountDto> countersMap = counterRepository.getCountersMap(userId);

        List<MailFolderResponse> tree = new ArrayList<>();

        Arrays.stream(SystemFolder.values())
                .filter(sf -> sf != SystemFolder.PURGED)
                .map(sf -> {
                    FolderCountDto counter = countersMap.get(sf.getId());
                    return fromSystemFolder(sf, counter);
                })
                .forEach(tree::add);

        folderRepository.findByOwnerIdOrderByParentFolderIdAscIdAsc(userId).stream()
                .map(folder -> {
                    FolderCountDto counter = countersMap.get(folder.getId());
                    return toResponseWithCounter(folder, counter);
                })
                .forEach(tree::add);

        return tree;
    }

    public List<FolderCounterResponse> getCounters(Long userId) {
        Map<Long, FolderCountDto> countersMap = counterRepository.getCountersMap(userId);
        return countersMap.values().stream()
                .map(c -> new FolderCounterResponse(
                        new MailFolderId(c.folderId()).toString(),
                        c.folderName(),
                        c.unread(),
                        c.total()))
                .toList();
    }

    public List<MailSummaryResponse> getMailsInFolder(Long userId, Long folderId,
            MailFolderMailsParams params) {
        validateFolderAccess(userId, folderId);
        var results = mailQueryRepository.findMailsInFolder(userId, folderId,
                params.offset(), params.getSize(), params.toSortField(), params.getKeyword(),
                params.getSdate(), params.getEdate());

        if (params.getKeyword() != null && !params.getKeyword().isBlank()) {
            results = highlightKeyword(results, params.getKeyword());
        }
        return results;
    }

    private List<MailSummaryResponse> highlightKeyword(List<MailSummaryResponse> results, String keyword) {
        var pattern = java.util.regex.Pattern.compile(
                "(" + java.util.regex.Pattern.quote(keyword) + ")",
                java.util.regex.Pattern.CASE_INSENSITIVE);
        return results.stream().map(r -> {
            var newAudit = new MailComponentDto.MailAuditInfoDto(
                    r.audit().createdBy(),
                    wrapMark(r.audit().createdByName(), pattern),
                    r.audit().createdDate(),
                    r.audit().updatedDate());
            var newSummary = new MailComponentDto.MailSummaryInfoDto(
                    r.summary().attachmentQty(),
                    wrapMark(r.summary().toStr(), pattern));
            return new MailSummaryResponse(
                    r.id(),
                    r.mailNumber(),
                    r.mailDate(),
                    wrapMark(r.subject(), pattern),
                    newAudit,
                    newSummary,
                    r.readStatus(),
                    r.folderId(),
                    r.type(),
                    r.category(),
                    r.circulationName(),
                    r.restoreFolder(),
                    r.thread(),
                    r.totalCount());
        }).toList();
    }

    private String wrapMark(String text, java.util.regex.Pattern pattern) {
        if (text == null || text.isBlank())
            return text;
        return pattern.matcher(text).replaceAll("<mark>$1</mark>");
    }

    private MailFolderResponse fromSystemFolder(SystemFolder sf, FolderCountDto counter) {
        MailFolderId parentId = sf.getParent() != null ? new MailFolderId(sf.getParent().getId()) : null;
        return new MailFolderResponse(
                new MailFolderId(sf.getId()),
                parentId,
                null,
                sf.getDisplayName(),
                "email",
                true,
                counter != null ? counter.unread() : 0L,
                counter != null ? counter.total() : 0L);
    }

    private MailFolderResponse toResponseWithCounter(MailFolder folder, FolderCountDto counter) {
        MailFolderId parentId = folder.getParentFolderId() != null 
                ? new MailFolderId(folder.getParentFolderId()) 
                : null;
        return new MailFolderResponse(
                new MailFolderId(folder.getId()),
                parentId,
                folder.getOwnerId() != null ? new UserId(folder.getOwnerId()) : null,
                folder.getName(),
                folder.getIconClsFolder(),
                false,
                counter != null ? counter.unread() : 0L,
                counter != null ? counter.total() : 0L);
    }

    private void validateFolderAccess(Long userId, Long folderId) {
        if (SystemFolder.findById(folderId).filter(sf -> sf != SystemFolder.PURGED).isPresent()) {
            return;
        }
        if (SystemFolder.isPersonalFolder(folderId)) {
            getOwnedPersonalFolder(userId, folderId);
            return;
        }
        throw new IllegalArgumentException("Invalid folder: " + folderId);
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
