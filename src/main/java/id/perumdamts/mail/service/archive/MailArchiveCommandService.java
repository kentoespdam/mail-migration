package id.perumdamts.mail.service.archive;

import id.perumdamts.mail.api.dto.archive.*;
import id.perumdamts.mail.config.TenantConfig;
import id.perumdamts.mail.domain.entity.ArchiveLocation;
import id.perumdamts.mail.domain.entity.MailArchive;
import id.perumdamts.mail.domain.entity.MailArchiveAccess;
import id.perumdamts.mail.domain.event.ArchivePublishedEvent;
import id.perumdamts.mail.infrastructure.security.MailPrincipal;
import id.perumdamts.mail.repository.jpa.MailArchiveAccessRepository;
import id.perumdamts.mail.repository.jpa.MailArchiveRepository;
import id.perumdamts.mail.repository.jpa.MailCategoryRepository;
import id.perumdamts.mail.service.archive.numbering.ArchiveNumberGenerator;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class MailArchiveCommandService {

    private final MailArchiveRepository archiveRepository;
    private final MailArchiveAccessRepository accessRepository;
    private final MailCategoryRepository categoryRepository;
    private final ArchiveNumberGenerator archiveNumberGenerator;
    private final ApplicationEventPublisher eventPublisher;
    private final TenantConfig tenantConfig;
    private final ArchiveMapper archiveMapper;

    public MailArchiveCommandService(MailArchiveRepository archiveRepository,
                                      MailArchiveAccessRepository accessRepository,
                                      MailCategoryRepository categoryRepository,
                                      ArchiveNumberGenerator archiveNumberGenerator,
                                      ApplicationEventPublisher eventPublisher,
                                      TenantConfig tenantConfig,
                                      ArchiveMapper archiveMapper) {
        this.archiveRepository = archiveRepository;
        this.accessRepository = accessRepository;
        this.categoryRepository = categoryRepository;
        this.archiveNumberGenerator = archiveNumberGenerator;
        this.eventPublisher = eventPublisher;
        this.tenantConfig = tenantConfig;
        this.archiveMapper = archiveMapper;
    }

    @Transactional
    public ArchiveResponse createDraft(ArchiveCreateRequest request, MailPrincipal principal) {
        var archive = new MailArchive();
        applyFields(archive, request);
        archive.setCreatedBy(Integer.parseInt(principal.userId()));
        archive.setCreatedByName(principal.name());
        archive.setCreatedDate(LocalDateTime.now());
        archive.setOfficeCode(tenantConfig.officeCode());

        archive = archiveRepository.save(archive);
        return archiveMapper.toResponse(archive);
    }

    @Transactional
    public ArchiveResponse updateDraft(Long archiveId, ArchiveUpdateRequest request, MailPrincipal principal) {
        var archive = getArchiveOrThrow(archiveId);
        if (!archive.isDraft()) {
            throw new IllegalStateException("Cannot update an archived record");
        }

        if (request.subject() != null) archive.setSubject(request.subject());
        if (request.content() != null) archive.setContent(request.content());
        if (request.archiveDate() != null) archive.setArchiveDate(request.archiveDate());
        if (request.year() != null) archive.setYear(request.year());
        if (request.keywordFlag() != null) archive.setKeywordFlag(request.keywordFlag());
        if (request.mailId() != null) archive.setMailId(request.mailId());
        if (request.categoryId() != null) {
            archive.setCategory(categoryRepository.getReferenceById(request.categoryId()));
        }
        if (request.rack() != null || request.shelf() != null
                || request.box() != null || request.folderPosition() != null) {
            var loc = archive.getLocation() != null ? archive.getLocation() : new ArchiveLocation();
            if (request.rack() != null) loc.setRack(request.rack());
            if (request.shelf() != null) loc.setShelf(request.shelf());
            if (request.box() != null) loc.setBox(request.box());
            if (request.folderPosition() != null) loc.setFolderPosition(request.folderPosition());
            archive.setLocation(loc);
        }

        archive.setUpdatedDate(LocalDateTime.now());
        return archiveMapper.toResponse(archiveRepository.save(archive));
    }

    @Transactional
    public ArchiveResponse publishArchive(Long archiveId, MailPrincipal principal) {
        var archive = getArchiveOrThrow(archiveId);
        if (!archive.isDraft()) {
            throw new IllegalStateException("Only draft archives can be published");
        }

        String archiveNumber = archiveNumberGenerator.generate(archive);
        archive.publish(archiveNumber);
        archiveRepository.save(archive);

        // Publish event for notifications
        List<Integer> accessPositionIds = accessRepository.findByArchiveId(archiveId)
                .stream()
                .map(MailArchiveAccess::getPositionId)
                .toList();
        eventPublisher.publishEvent(new ArchivePublishedEvent(
                archiveId,
                Integer.parseInt(principal.userId()),
                principal.name(),
                archive.getOfficeCode(),
                accessPositionIds));

        return archiveMapper.toResponse(archive);
    }

    @Transactional
    public void deleteArchive(Long archiveId, MailPrincipal principal) {
        var archive = getArchiveOrThrow(archiveId);
        archive.softDelete();
        archiveRepository.save(archive);
    }

    /**
     * Atomic set access: delete all existing + insert batch (fix B6).
     */
    @Transactional
    public List<ArchiveAccessResponse> setAccess(Long archiveId, ArchiveAccessRequest request,
                                                   MailPrincipal principal) {
        getArchiveOrThrow(archiveId); // verify exists

        Integer grantedBy = Integer.parseInt(principal.userId());
        accessRepository.deleteByArchiveId(archiveId);

        List<MailArchiveAccess> entries = request.entries().stream()
                .map(e -> MailArchiveAccess.create(archiveId, e.positionId(), e.accessLevel(), grantedBy))
                .toList();
        accessRepository.saveAll(entries);

        return entries.stream().map(archiveMapper::toAccessResponse).toList();
    }

    public List<ArchiveAccessResponse> getAccess(Long archiveId) {
        return accessRepository.findByArchiveId(archiveId)
                .stream()
                .map(archiveMapper::toAccessResponse)
                .toList();
    }

    private void applyFields(MailArchive archive, ArchiveCreateRequest request) {
        archive.setSubject(request.subject());
        archive.setContent(request.content());
        archive.setArchiveDate(request.archiveDate());
        archive.setYear(request.year());
        archive.setMailId(request.mailId());
        archive.setKeywordFlag(request.keywordFlag());
        if (request.categoryId() != null) {
            archive.setCategory(categoryRepository.getReferenceById(request.categoryId()));
        }
        archive.setLocation(new ArchiveLocation(
                request.rack(), request.shelf(), request.box(), request.folderPosition()));
    }

    private MailArchive getArchiveOrThrow(Long archiveId) {
        return archiveRepository.findById(archiveId)
                .orElseThrow(() -> new EntityNotFoundException("Archive not found: " + archiveId));
    }
}
