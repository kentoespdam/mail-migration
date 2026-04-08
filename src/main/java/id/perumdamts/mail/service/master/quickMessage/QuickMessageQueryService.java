package id.perumdamts.mail.service.master.quickMessage;

import id.perumdamts.mail.config.CacheConfig;
import id.perumdamts.mail.dto.master.quickMessage.QuickMessageMapper;
import id.perumdamts.mail.dto.master.quickMessage.QuickMessageParams;
import id.perumdamts.mail.dto.master.quickMessage.QuickMessageResponse;
import id.perumdamts.mail.enums.RecordStatus;
import id.perumdamts.mail.repository.master.jooq.QuickMessageQueryRepository;
import id.perumdamts.mail.repository.master.jpa.QuickMessageRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class QuickMessageQueryService {

    private final QuickMessageQueryRepository repository;
    private final QuickMessageRepository jpaRepository;
    private final QuickMessageMapper mapper;

    public Page<QuickMessageResponse> findAll(QuickMessageParams params) {
        return repository.findAll(params);
    }

    public QuickMessageResponse findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("QuickMessage not found: " + id));
    }

    @Cacheable(value = CacheConfig.CacheNames.TENANT_CONFIG, key = "'quickMessages'")
    public List<QuickMessageResponse> lookup() {
        return jpaRepository.findAllByStatusOrderByMessageAsc(RecordStatus.ACTIVE).stream()
                .map(mapper::toResponse)
                .toList();
    }
}
