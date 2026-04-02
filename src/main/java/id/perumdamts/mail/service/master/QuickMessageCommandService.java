package id.perumdamts.mail.service.master;

import id.perumdamts.mail.config.CacheConfig;
import id.perumdamts.mail.dto.master.quickMessage.QuickMessageRequest;
import id.perumdamts.mail.dto.master.quickMessage.QuickMessageResponse;
import id.perumdamts.mail.entity.master.QuickMessage;
import id.perumdamts.mail.repository.master.jpa.QuickMessageRepository;
import id.perumdamts.mail.repository.master.jooq.QuickMessageQueryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class QuickMessageCommandService {

    private final QuickMessageRepository repository;
    private final QuickMessageQueryRepository queryRepository;

    @CacheEvict(value = CacheConfig.CacheNames.TENANT_CONFIG, key = "'quickMessages'")
    public QuickMessageResponse create(QuickMessageRequest request) {
        String trimmed = request.message().trim();
        if (repository.existsByMessage(trimmed)) {
            throw new IllegalArgumentException(
                    "Pesan '%s' sudah ada, silahkan gunakan pesan yang lain.".formatted(trimmed));
        }
        var entity = new QuickMessage(trimmed);
        var saved = repository.save(entity);
        return queryRepository.findById(saved.getId()).orElseThrow();
    }

    @CacheEvict(value = CacheConfig.CacheNames.TENANT_CONFIG, key = "'quickMessages'")
    public QuickMessageResponse update(Long id, QuickMessageRequest request) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("QuickMessage not found: " + id));
        String trimmed = request.message().trim();
        if (repository.existsByMessageAndIdNot(trimmed, id)) {
            throw new IllegalArgumentException(
                    "Pesan '%s' sudah ada, silahkan gunakan pesan yang lain.".formatted(trimmed));
        }
        entity.setMessage(trimmed);
        repository.save(entity);
        return queryRepository.findById(id).orElseThrow();
    }

    @CacheEvict(value = CacheConfig.CacheNames.TENANT_CONFIG, key = "'quickMessages'")
    public void delete(Long id) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("QuickMessage not found: " + id));
        entity.markDeleted();
        repository.save(entity);
    }
}
