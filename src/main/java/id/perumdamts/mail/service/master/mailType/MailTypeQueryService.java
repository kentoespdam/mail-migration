package id.perumdamts.mail.service.master.mailType;

import id.perumdamts.mail.config.CacheConfig;
import id.perumdamts.mail.dto.master.mailType.MailTypeLookup;
import id.perumdamts.mail.dto.master.mailType.MailTypeMapper;
import id.perumdamts.mail.dto.master.mailType.MailTypeParams;
import id.perumdamts.mail.dto.master.mailType.MailTypeResponse;
import id.perumdamts.mail.enums.RecordStatus;
import id.perumdamts.mail.repository.master.jooq.MailTypeQueryRepository;
import id.perumdamts.mail.repository.master.jpa.MailTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MailTypeQueryService {
    private final MailTypeQueryRepository repository;
    private final MailTypeRepository jpaRepository;
    private final MailTypeMapper mapper;

    public Page<MailTypeResponse> findAll(MailTypeParams params) {
        return repository.findAll(params);
    }

    @org.springframework.cache.annotation.Cacheable(value = CacheConfig.CacheNames.MAIL_TYPES, key = "'lookup'")
    public List<MailTypeLookup> lookup() {
        return jpaRepository.findAllByStatusOrderByIdAsc(RecordStatus.ACTIVE).stream()
                .map(mapper::toLookup)
                .toList();
    }

    public MailTypeResponse findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MailType not found: " + id));
    }
}
