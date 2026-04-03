package id.perumdamts.mail.service.core.publication;

import id.perumdamts.mail.config.StorageProperties;
import id.perumdamts.mail.dto.common.FileDownloadResource;
import id.perumdamts.mail.dto.core.publication.PublicationParams;
import id.perumdamts.mail.dto.core.publication.PublicationResponse;
import id.perumdamts.mail.entity.core.Publication;
import id.perumdamts.mail.repository.core.jpa.PublicationRepository;
import id.perumdamts.mail.repository.core.jooq.PublicationQueryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class PublicationQueryService {

    private final PublicationQueryRepository queryRepository;
    private final PublicationRepository publicationRepository;
    private final StorageProperties storageProperties;

    public Page<PublicationResponse> findAll(PublicationParams params) {
        List<PublicationResponse> items = queryRepository.findAll(params);
        long total = items.stream()
                .findFirst()
                .map(PublicationResponse::getTotalCount)
                .map(Integer::longValue)
                .orElse((long) items.size());
        return new PageImpl<>(items, PageRequest.of(params.getPage(), params.getSize()), total);
    }

    public PublicationResponse findById(Long id) {
        return queryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Publication not found: " + id));
    }

    public FileDownloadResource downloadFile(Long id) {
        Publication pub = publicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Publication not found: " + id));

        if (pub.getFilePath() == null) {
            throw new EntityNotFoundException("File not found for publication: " + id);
        }

        try {
            Path basePath = Paths.get(storageProperties.basePath()).normalize();
            Path path = basePath.resolve(pub.getFilePath()).normalize();
            if (!path.startsWith(basePath)) {
                throw new SecurityException("Invalid file path");
            }

            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists()) {
                throw new EntityNotFoundException("Physical file not found: " + pub.getFilePath());
            }

            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return new FileDownloadResource(pub.getFileName(), contentType, resource);
        } catch (IOException e) {
            throw new RuntimeException("Failed to resolve file path", e);
        }
    }
}
