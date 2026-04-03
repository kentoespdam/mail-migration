package id.perumdamts.mail.service.core.publication;

import id.perumdamts.mail.config.StorageProperties;
import id.perumdamts.mail.dto.core.publication.PublicationDownloadResult;
import id.perumdamts.mail.dto.core.publication.PublicationParams;
import id.perumdamts.mail.dto.core.publication.PublicationResponse;
import id.perumdamts.mail.repository.core.jpa.PublicationRepository;
import id.perumdamts.mail.repository.core.jooq.PublicationQueryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Transactional(readOnly = true)
@Slf4j
public class PublicationQueryService {

    private final PublicationQueryRepository queryRepository;
    private final PublicationRepository publicationRepository;
    private final Path storagePath;

    public PublicationQueryService(PublicationQueryRepository queryRepository,
                                   PublicationRepository publicationRepository,
                                   StorageProperties storageProperties) {
        this.queryRepository = queryRepository;
        this.publicationRepository = publicationRepository;
        this.storagePath = Paths.get(storageProperties.basePath()).toAbsolutePath().normalize();
    }

    public Page<PublicationResponse> findAll(PublicationParams params) {
        return queryRepository.findAll(params);
    }

    public PublicationResponse findById(Long id) {
        return queryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Publication not found: " + id));
    }

    public PublicationDownloadResult download(Long id) {
        var entity = publicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Publication not found: " + id));

        if (entity.getFilePath() == null || entity.getFilePath().isBlank()) {
            throw new IllegalStateException("Publication has no file: " + id);
        }

        Path filePath = storagePath.resolve(entity.getFilePath()).normalize();
        if (!filePath.startsWith(storagePath)) {
            throw new IllegalStateException("Invalid stored file path");
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                throw new IllegalStateException("File not found on disk: " + entity.getFilePath());
            }
            return new PublicationDownloadResult(entity.getFileName(), resource);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Malformed file path: " + filePath, e);
        }
    }
}
