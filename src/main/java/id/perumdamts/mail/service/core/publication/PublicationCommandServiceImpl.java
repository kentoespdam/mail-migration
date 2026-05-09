package id.perumdamts.mail.service.core.publication;

import id.perumdamts.mail.config.CacheConfig;
import id.perumdamts.mail.dto.core.publication.CreatePublicationRequest;
import id.perumdamts.mail.dto.core.publication.PublicationCommandResult;
import id.perumdamts.mail.dto.core.publication.UpdatePublicationRequest;
import id.perumdamts.mail.dto.id.PublicationId;
import id.perumdamts.mail.entity.core.Publication;
import id.perumdamts.mail.event.PublicationPublishedEvent;
import id.perumdamts.mail.repository.core.jpa.PublicationRepository;
import id.perumdamts.mail.repository.master.jpa.DocumentTypeRepository;
import id.perumdamts.mail.security.MailPrincipal;
import id.perumdamts.mail.service.master.allowedFileType.AllowedFileTypeService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@org.springframework.cache.annotation.CacheConfig(cacheNames = CacheConfig.CacheNames.PUBLICATIONS)
public class PublicationCommandServiceImpl implements PublicationCommandHandler {

    private final PublicationRepository publicationRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final AllowedFileTypeService allowedFileTypeService;
    private final ApplicationEventPublisher eventPublisher;
    private final PublicationFileStorageService fileStorageService;

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(allEntries = true)
    public PublicationCommandResult create(CreatePublicationRequest request, MailPrincipal principal) {
        var pub = new Publication();
        pub.setTitle(request.getTitle());
        pub.setDescription(request.getDescription());
        pub.setDocumentType(documentTypeRepository.getReferenceById(request.getDocumentTypeId().value()));
        pub.setCreatedByUserId(Integer.parseInt(principal.userId()));
        pub.setCreatedByName(principal.name());
        pub.setCreatedAt(LocalDateTime.now());

        var file = request.getFile();
        if (file != null && !file.isEmpty()) {
            allowedFileTypeService.validate("PUBLICATION", file);
            var stored = fileStorageService.store(file);
            pub.setOriginalFileName(stored.originalFileName());
            pub.setSystemFileName(stored.systemFileName());
            pub.setFileSize((int) stored.fileSize());
        }

        boolean shouldPublish = request.isPublish() && pub.publish();

        pub = publicationRepository.save(pub);

        if (shouldPublish) {
            eventPublisher.publishEvent(new PublicationPublishedEvent(pub.getId(), principal.name()));
        }

        return toResult(pub);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(allEntries = true)
    public PublicationCommandResult update(Long id, UpdatePublicationRequest request, MailPrincipal principal) {
        var pub = getOrThrow(id);

        pub.setTitle(request.getTitle());
        pub.setDescription(request.getDescription());
        pub.setDocumentType(documentTypeRepository.getReferenceById(request.getDocumentTypeId().value()));
        pub.setUpdatedAt(LocalDateTime.now());

        var file = request.getFile();
        if (file != null && !file.isEmpty()) {
            allowedFileTypeService.validate("PUBLICATION", file);
            fileStorageService.delete(pub.getSystemFileName(), pub.getCreatedAt());
            var stored = fileStorageService.store(file);
            pub.setOriginalFileName(stored.originalFileName());
            pub.setSystemFileName(stored.systemFileName());
            pub.setFileSize((int) stored.fileSize());
        }

        boolean shouldPublish = request.isPublish() && pub.publish();

        pub = publicationRepository.save(pub);

        if (shouldPublish) {
            eventPublisher.publishEvent(new PublicationPublishedEvent(pub.getId(), principal.name()));
        }

        return toResult(pub);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(allEntries = true)
    public void delete(Long id, MailPrincipal principal) {
        var pub = getOrThrow(id);
        pub.softDelete();
        publicationRepository.save(pub);
        log.info("Publication soft-deleted: id={}, by={}", id, principal.name());
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(allEntries = true)
    public PublicationCommandResult publish(Long id, MailPrincipal principal) {
        var pub = getOrThrow(id);
        if (pub.publish()) {
            pub = publicationRepository.save(pub);
            eventPublisher.publishEvent(new PublicationPublishedEvent(pub.getId(), principal.name()));
        }

        return toResult(pub);
    }

    private PublicationCommandResult toResult(Publication pub) {
        return new PublicationCommandResult(
                new PublicationId(pub.getId()),
                pub.getStatus().name(),
                pub.getUpdatedAt() != null ? pub.getUpdatedAt() : pub.getCreatedAt()
        );
    }

    private Publication getOrThrow(Long id) {
        return publicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Publication not found: " + id));
    }
}
