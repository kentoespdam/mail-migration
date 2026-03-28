package id.perumdamts.mail.service.publication;

import id.perumdamts.mail.api.dto.publication.AllowedFileTypeDto;
import id.perumdamts.mail.api.dto.publication.AllowedFileTypeRequest;
import id.perumdamts.mail.api.dto.publication.PublicationMapper;
import id.perumdamts.mail.domain.entity.AllowedFileType;
import id.perumdamts.mail.repository.jpa.AllowedFileTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AllowedFileTypeService {

    private final AllowedFileTypeRepository repository;
    private final PublicationMapper mapper;

    public AllowedFileTypeService(AllowedFileTypeRepository repository,
                                   PublicationMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Cacheable(value = "allowedFileTypes", key = "#context")
    public List<AllowedFileTypeDto> listByContext(String context) {
        return repository.findByContextAndIsActiveTrue(context).stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<AllowedFileTypeDto> listAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    @CacheEvict(value = "allowedFileTypes", allEntries = true)
    public AllowedFileTypeDto create(AllowedFileTypeRequest request) {
        var entity = new AllowedFileType();
        entity.setContext(request.context().toUpperCase());
        entity.setExtension(request.extension().toLowerCase());
        entity.setMaxSizeMb(request.maxSizeMb());
        entity.setIsActive(request.isActive() != null ? request.isActive() : true);
        return mapper.toDto(repository.save(entity));
    }

    @Transactional
    @CacheEvict(value = "allowedFileTypes", allEntries = true)
    public AllowedFileTypeDto update(Integer id, AllowedFileTypeRequest request) {
        var entity = getOrThrow(id);
        entity.setContext(request.context().toUpperCase());
        entity.setExtension(request.extension().toLowerCase());
        entity.setMaxSizeMb(request.maxSizeMb());
        if (request.isActive() != null) {
            entity.setIsActive(request.isActive());
        }
        return mapper.toDto(repository.save(entity));
    }

    @Transactional
    @CacheEvict(value = "allowedFileTypes", allEntries = true)
    public void delete(Integer id) {
        var entity = getOrThrow(id);
        entity.setIsActive(false);
        repository.save(entity);
    }

    public void validate(String context, MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String ext = extractExtension(originalFilename);

        List<AllowedFileType> rules = repository.findByContextAndIsActiveTrue(context);

        AllowedFileType matched = rules.stream()
                .filter(r -> r.getExtension().equalsIgnoreCase(ext))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "File type '." + ext + "' is not allowed for context: " + context));

        long fileSizeMb = file.getSize() / (1024 * 1024);
        if (fileSizeMb > matched.getMaxSizeMb()) {
            throw new IllegalArgumentException(
                    "File size exceeds maximum " + matched.getMaxSizeMb() + " MB for '." + ext + "'");
        }
    }

    private AllowedFileType getOrThrow(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("AllowedFileType not found: " + id));
    }

    private String extractExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1).toLowerCase() : "";
    }
}
