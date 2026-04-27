package id.perumdamts.mail.service.core.publication;

import id.perumdamts.mail.config.CacheConfig;
import id.perumdamts.mail.dto.core.publication.PublicationDownloadResult;
import id.perumdamts.mail.dto.core.publication.PublicationParams;
import id.perumdamts.mail.dto.core.publication.PublicationResponse;
import id.perumdamts.mail.repository.core.jooq.PublicationQueryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@org.springframework.cache.annotation.CacheConfig(cacheNames = CacheConfig.CacheNames.PUBLICATIONS)
public class PublicationQueryServiceImpl implements PublicationQueryHandler {

    private final PublicationQueryRepository queryRepository;
    private final PublicationFileStorageService fileStorageService;

    @Override
    @Cacheable(key = "#params")
    public Page<PublicationResponse> findAll(PublicationParams params) {
        return queryRepository.findAll(params);
    }

    @Override
    @Cacheable(key = "#id")
    public PublicationResponse findById(Long id) {
        return queryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Publication not found: " + id));
    }

    @Override
    public PublicationDownloadResult download(Long id) {
        var meta = queryRepository.findFileMeta(id)
                .orElseThrow(() -> new EntityNotFoundException("Publication not found: " + id));

        if (meta.systemFileName() == null || meta.systemFileName().isBlank()) {
            throw new IllegalStateException("Publication has no file: " + id);
        }

        var resource = fileStorageService.load(meta.systemFileName(), meta.createdAt());
        return new PublicationDownloadResult(meta.originalFileName(), resource);
    }
}
