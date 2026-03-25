package id.perumdamts.mail.service.master;

import id.perumdamts.mail.api.dto.master.QuickMessageMapper;
import id.perumdamts.mail.api.dto.master.QuickMessageRequest;
import id.perumdamts.mail.api.dto.master.QuickMessageResponse;
import id.perumdamts.mail.config.CacheConfig;
import id.perumdamts.mail.domain.entity.QuickMessage;
import id.perumdamts.mail.domain.enums.RecordStatus;
import id.perumdamts.mail.repository.jpa.QuickMessageRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class QuickMessageService {

    private final QuickMessageRepository repository;
    private final QuickMessageMapper mapper;

    public QuickMessageService(QuickMessageRepository repository, QuickMessageMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * Paginated list — untuk admin panel (termasuk INACTIVE, exclude DELETED via @SQLRestriction).
     */
    public Page<QuickMessageResponse> findAll(Pageable pageable) {
        return repository.findAllByOrderByMessageAsc(pageable)
                .map(mapper::toResponse);
    }

    public QuickMessageResponse findById(Integer id) {
        return mapper.toResponse(getOrThrow(id));
    }

    /**
     * Lookup aktif saja — cached di Redis (TTL 6 jam).
     * Dipakai untuk dropdown di frontend. Sorted alphabetical.
     */
    @Cacheable(value = CacheConfig.CacheNames.TENANT_CONFIG, key = "'quickMessages'")
    public List<QuickMessageResponse> lookup() {
        return repository.findAllByStatusOrderByMessageAsc(RecordStatus.ACTIVE).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    @CacheEvict(value = CacheConfig.CacheNames.TENANT_CONFIG, key = "'quickMessages'")
    public QuickMessageResponse create(QuickMessageRequest request) {
        String trimmed = request.message().trim();
        if (repository.existsByMessage(trimmed)) {
            throw new IllegalArgumentException(
                    "Pesan '%s' sudah ada, silahkan gunakan pesan yang lain.".formatted(trimmed));
        }
        var entity = new QuickMessage(trimmed);
        return mapper.toResponse(repository.save(entity));
    }

    @Transactional
    @CacheEvict(value = CacheConfig.CacheNames.TENANT_CONFIG, key = "'quickMessages'")
    public QuickMessageResponse update(Integer id, QuickMessageRequest request) {
        var entity = getOrThrow(id);
        String trimmed = request.message().trim();
        if (repository.existsByMessageAndIdNot(trimmed, id)) {
            throw new IllegalArgumentException(
                    "Pesan '%s' sudah ada, silahkan gunakan pesan yang lain.".formatted(trimmed));
        }
        entity.setMessage(trimmed);
        return mapper.toResponse(repository.save(entity));
    }

    @Transactional
    @CacheEvict(value = CacheConfig.CacheNames.TENANT_CONFIG, key = "'quickMessages'")
    public void delete(Integer id) {
        var entity = getOrThrow(id);
        entity.markDeleted();
        repository.save(entity);
    }

    private QuickMessage getOrThrow(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("QuickMessage not found: " + id));
    }
}
