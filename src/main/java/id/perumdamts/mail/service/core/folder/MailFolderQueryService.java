package id.perumdamts.mail.service.core.folder;

import id.perumdamts.mail.config.CacheConfig;
import id.perumdamts.mail.dto.core.folder.FolderCountDto;
import id.perumdamts.mail.dto.core.folder.FolderCounterResponse;
import id.perumdamts.mail.dto.core.folder.MailFolderMailsParams;
import id.perumdamts.mail.dto.core.folder.MailFolderResponse;
import id.perumdamts.mail.dto.core.mail.MailComponentDto;
import id.perumdamts.mail.dto.core.mail.MailSummaryResponse;
import id.perumdamts.mail.entity.core.MailFolder;
import id.perumdamts.mail.enums.SystemFolder;
import id.perumdamts.mail.repository.core.jooq.FolderCounterRepository;
import id.perumdamts.mail.repository.core.jooq.MailQueryRepository;
import id.perumdamts.mail.repository.core.jpa.MailFolderRepository;
import id.perumdamts.mail.util.SqidsEncoder;
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
    private final SqidsEncoder encoder;

    public MailFolderQueryService(MailFolderRepository folderRepository,
            FolderCounterRepository counterRepository,
            MailQueryRepository mailQueryRepository,
            SqidsEncoder encoder) {
        this.folderRepository = folderRepository;
        this.counterRepository = counterRepository;
        this.mailQueryRepository = mailQueryRepository;
        this.encoder = encoder;
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
                        encoder.encode(MailFolder.class, c.folderId()),
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
                    r.getAudit().createdBy(),
                    wrapMark(r.getAudit().createdByName(), pattern),
                    r.getAudit().createdDate(),
                    r.getAudit().updatedDate());
            var newSummary = new MailComponentDto.MailSummaryInfoDto(
                    r.getSummary().attachmentQty(),
                    wrapMark(r.getSummary().toStr(), pattern));
            return new MailSummaryResponse(
                    r.getId(),
                    r.getMailNumber(),
                    r.getMailDate(),
                    wrapMark(r.getSubject(), pattern),
                    newAudit,
                    newSummary,
                    r.getReadStatus(),
                    r.getFolderId(),
                    r.getType(),
                    r.getCategory(),
                    r.getCirculationName(),
                    r.getRestoreFolder(),
                    r.getThread(),
                    r.getTotalCount());
        }).toList();
    }

    private String wrapMark(String text, java.util.regex.Pattern pattern) {
        if (text == null || text.isBlank())
            return text;
        return pattern.matcher(text).replaceAll("<mark>$1</mark>");
    }

    private MailFolderResponse fromSystemFolder(SystemFolder sf, FolderCountDto counter) {
        long parentFolderId = sf.getParent() != null ? sf.getParent().getId() : 0L;
        return new MailFolderResponse(
                encoder.encode(MailFolder.class, sf.getId()),
                parentFolderId > 0 ? encoder.encode(MailFolder.class, parentFolderId) : "0",
                "0",
                sf.getDisplayName(),
                "email",
                true,
                counter != null ? counter.unread() : 0L,
                counter != null ? counter.total() : 0L);
    }

    private MailFolderResponse toResponseWithCounter(MailFolder folder, FolderCountDto counter) {
        String parentId = folder.getParentFolderId() != null && folder.getParentFolderId() > 0 
                ? encoder.encode(MailFolder.class, folder.getParentFolderId()) 
                : "0";
        return new MailFolderResponse(
                encoder.encode(MailFolder.class, folder.getId()),
                parentId,
                encoder.encode(MailFolder.class, folder.getOwnerId()),
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
