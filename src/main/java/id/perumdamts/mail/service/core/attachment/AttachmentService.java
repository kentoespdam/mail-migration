package id.perumdamts.mail.service.core.attachment;

import id.perumdamts.mail.config.StorageProperties;
import id.perumdamts.mail.dto.core.attachment.AttachmentMapper;
import id.perumdamts.mail.dto.core.attachment.AttachmentResponse;
import id.perumdamts.mail.entity.core.Attachment;
import id.perumdamts.mail.entity.core.AttachmentDownloadHistory;
import id.perumdamts.mail.enums.AttachmentRefType;
import id.perumdamts.mail.repository.core.jpa.AttachmentDownloadHistoryRepository;
import id.perumdamts.mail.repository.core.jpa.AttachmentRepository;
import id.perumdamts.mail.security.MailPrincipal;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AttachmentService {

    private static final Logger log = LoggerFactory.getLogger(AttachmentService.class);

    private final AttachmentRepository repository;
    private final AttachmentDownloadHistoryRepository historyRepository;
    private final AttachmentMapper mapper;
    private final Path storagePath;

    public AttachmentService(AttachmentRepository repository,
                             AttachmentDownloadHistoryRepository historyRepository,
                             AttachmentMapper mapper,
                             StorageProperties storageProperties) {
        this.repository = repository;
        this.historyRepository = historyRepository;
        this.mapper = mapper;
        this.storagePath = Paths.get(storageProperties.basePath()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storagePath);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create storage directory: " + this.storagePath, e);
        }
    }

    public List<AttachmentResponse> findByOwner(AttachmentRefType refType, Long refId) {
        return repository.findAllByRefTypeAndRefId(refType.getDbValue(), refId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public AttachmentResponse findById(Integer id) {
        return mapper.toResponse(getOrThrow(id));
    }

    @Transactional
    public AttachmentResponse upload(MultipartFile file, AttachmentRefType refType, Long refId,
                                     String docNotes, MailPrincipal principal) {
        String originalFilename = file.getOriginalFilename();
        String fileExt = extractExtension(originalFilename);
        String systemFilename = UUID.randomUUID() + (fileExt.isEmpty() ? "" : "." + fileExt);

        // Save file to disk
        Path targetPath = storagePath.resolve(systemFilename).normalize();
        if (!targetPath.startsWith(storagePath)) {
            throw new IllegalArgumentException("Invalid file path");
        }
        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file: " + originalFilename, e);
        }

        var entity = new Attachment(refType, refId, originalFilename, systemFilename,
                fileExt, (int) file.getSize(), principal.name());
        if (docNotes != null) {
            entity.setDocNotes(docNotes);
        }
        return mapper.toResponse(repository.save(entity));
    }

    /**
     * Returns the file as a {@link Resource} for streaming download.
     * Also logs the download in {@code attachment_download_history}.
     */
    @Transactional
    public DownloadResult download(Integer id, MailPrincipal principal) {
        var attachment = getOrThrow(id);

        // Log download history
        historyRepository.save(new AttachmentDownloadHistory(
                attachment.getId(),
                parseUserId(principal.userId()),
                principal.name(),
                null
        ));

        Path filePath = storagePath.resolve(attachment.getSystemFilename()).normalize();
        if (!filePath.startsWith(storagePath)) {
            throw new IllegalStateException("Invalid stored file path");
        }
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                throw new IllegalStateException("File not found on disk: " + attachment.getSystemFilename());
            }
            return new DownloadResult(resource, attachment.getOriginalFilename(), attachment.getFileSize());
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Malformed file path: " + filePath, e);
        }
    }

    @Transactional
    public void delete(Integer id) {
        var entity = getOrThrow(id);
        entity.markDeleted();
        repository.save(entity);
    }

    // ── Helpers ──

    private Attachment getOrThrow(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found: " + id));
    }

    private String extractExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1).toLowerCase() : "";
    }

    private Integer parseUserId(String userId) {
        try {
            return Integer.parseInt(userId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public record DownloadResult(Resource resource, String filename, Integer fileSize) {}
}
