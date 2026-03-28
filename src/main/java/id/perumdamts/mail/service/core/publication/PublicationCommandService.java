package id.perumdamts.mail.service.core.publication;

import id.perumdamts.mail.config.StorageProperties;
import id.perumdamts.mail.dto.core.publication.CreatePublicationRequest;
import id.perumdamts.mail.dto.core.publication.PublicationDto;
import id.perumdamts.mail.dto.core.publication.PublicationMapper;
import id.perumdamts.mail.dto.core.publication.UpdatePublicationRequest;
import id.perumdamts.mail.entity.core.Publication;
import id.perumdamts.mail.event.PublicationPublishedEvent;
import id.perumdamts.mail.repository.core.jpa.PublicationRepository;
import id.perumdamts.mail.repository.master.jpa.DocumentTypeRepository;
import id.perumdamts.mail.security.MailPrincipal;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PublicationCommandService {

    private static final Logger log = LoggerFactory.getLogger(PublicationCommandService.class);
    private static final DateTimeFormatter MONTH_DIR = DateTimeFormatter.ofPattern("yyyyMM");

    private final PublicationRepository publicationRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final AllowedFileTypeService allowedFileTypeService;
    private final ApplicationEventPublisher eventPublisher;
    private final PublicationMapper mapper;
    private final Path storagePath;

    public PublicationCommandService(PublicationRepository publicationRepository,
                                      DocumentTypeRepository documentTypeRepository,
                                      AllowedFileTypeService allowedFileTypeService,
                                      ApplicationEventPublisher eventPublisher,
                                      PublicationMapper mapper,
                                      StorageProperties storageProperties) {
        this.publicationRepository = publicationRepository;
        this.documentTypeRepository = documentTypeRepository;
        this.allowedFileTypeService = allowedFileTypeService;
        this.eventPublisher = eventPublisher;
        this.mapper = mapper;
        this.storagePath = Paths.get(storageProperties.basePath(), "publik").toAbsolutePath().normalize();
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public PublicationDto create(CreatePublicationRequest request, MultipartFile file,
                                  MailPrincipal principal) {
        var pub = new Publication();
        pub.setTitle(request.title());
        pub.setDescription(request.description());
        pub.setDocumentType(documentTypeRepository.getReferenceById(request.documentTypeId()));
        pub.setCreatedByUserId(Integer.parseInt(principal.userId()));
        pub.setCreatedByName(principal.name());
        pub.setCreatedAt(LocalDateTime.now());

        if (file != null && !file.isEmpty()) {
            allowedFileTypeService.validate("PUBLICATION", file);
            storeFile(pub, file);
        }

        if (request.publish()) {
            pub.publish();
        }

        pub = publicationRepository.save(pub);

        if (request.publish()) {
            eventPublisher.publishEvent(new PublicationPublishedEvent(pub.getId(), principal.name()));
        }

        return mapper.toDto(pub);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public PublicationDto update(Integer id, UpdatePublicationRequest request, MultipartFile file,
                                  MailPrincipal principal) {
        var pub = getOrThrow(id);

        pub.setTitle(request.title());
        pub.setDescription(request.description());
        pub.setDocumentType(documentTypeRepository.getReferenceById(request.documentTypeId()));
        pub.setUpdatedAt(LocalDateTime.now());

        if (file != null && !file.isEmpty()) {
            allowedFileTypeService.validate("PUBLICATION", file);
            deleteOldFile(pub);
            storeFile(pub, file);
        }

        boolean wasPublished = pub.isPublished();
        if (request.publish() && pub.isDraft()) {
            pub.publish();
        }

        pub = publicationRepository.save(pub);

        if (request.publish() && !wasPublished) {
            eventPublisher.publishEvent(new PublicationPublishedEvent(pub.getId(), principal.name()));
        }

        return mapper.toDto(pub);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Integer id, MailPrincipal principal) {
        var pub = getOrThrow(id);
        pub.softDelete();
        publicationRepository.save(pub);
        log.info("Publication soft-deleted: id={}, by={}", id, principal.name());
    }

    // ── Helpers ──

    private void storeFile(Publication pub, MultipartFile file) {
        String monthDir = LocalDateTime.now().format(MONTH_DIR);
        String ext = extractExtension(file.getOriginalFilename());
        String systemFilename = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
        Path dir = storagePath.resolve(monthDir);

        try {
            Files.createDirectories(dir);
            Path target = dir.resolve(systemFilename).normalize();
            if (!target.startsWith(storagePath)) {
                throw new IllegalArgumentException("Invalid file path");
            }
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            pub.setFileName(file.getOriginalFilename());
            pub.setFilePath("publik/" + monthDir + "/" + systemFilename);
            pub.setFileSize((int) file.getSize());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file: " + file.getOriginalFilename(), e);
        }
    }

    private void deleteOldFile(Publication pub) {
        if (pub.getFilePath() != null) {
            try {
                Path basePath = storagePath.getParent(); // back to base-path
                Path oldFile = basePath.resolve(pub.getFilePath()).normalize();
                if (oldFile.startsWith(basePath)) {
                    Files.deleteIfExists(oldFile);
                }
            } catch (IOException e) {
                log.warn("Failed to delete old file: {}", pub.getFilePath(), e);
            }
        }
    }

    private Publication getOrThrow(Integer id) {
        return publicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Publication not found: " + id));
    }

    private String extractExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1).toLowerCase() : "";
    }
}
