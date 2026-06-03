package id.perumdamts.mail.service.master.mailCategory;

import id.perumdamts.mail.config.CacheConfig;
import id.perumdamts.mail.dto.master.mailCategory.MailCategoryLookup;
import id.perumdamts.mail.dto.master.mailCategory.MailCategoryMapper;
import id.perumdamts.mail.dto.master.mailCategory.MailCategoryParams;
import id.perumdamts.mail.dto.master.mailCategory.MailCategoryResponse;
import id.perumdamts.mail.enums.CategoryStatus;
import id.perumdamts.mail.repository.master.jooq.MailCategoryQueryRepository;
import id.perumdamts.mail.repository.master.jpa.MailCategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MailCategoryQueryService {
    private final MailCategoryQueryRepository repository;
    private final MailCategoryRepository jpaRepository;
    private final MailCategoryMapper mapper;

    public Page<MailCategoryResponse> findAll(MailCategoryParams params) {
        return repository.findAll(params);
    }

    public MailCategoryResponse findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MailCategory not found: " + id));
    }
    @org.springframework.cache.annotation.Cacheable(value = CacheConfig.CacheNames.MAIL_CATEGORIES, key = "'lookup'")
    public java.util.List<MailCategoryLookup> lookup() {
        return jpaRepository.findAllByStatusOrderByIdAsc(CategoryStatus.ENABLED).stream()
                .map(mapper::toLookup)
                .toList();
    }
}
