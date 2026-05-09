package id.perumdamts.mail.service.master.mailCategory;

import id.perumdamts.mail.config.CacheConfig;
import id.perumdamts.mail.dto.master.mailCategory.MailCategoryRequest;
import id.perumdamts.mail.dto.master.mailCategory.MailCategoryResponse;
import id.perumdamts.mail.entity.master.MailCategory;
import id.perumdamts.mail.entity.master.MailType;
import id.perumdamts.mail.repository.master.jooq.MailCategoryQueryRepository;
import id.perumdamts.mail.repository.master.jpa.MailCategoryRepository;
import id.perumdamts.mail.repository.master.jpa.MailTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MailCategoryCommandService {

    private final MailCategoryRepository repository;
    private final MailTypeRepository mailTypeRepository;
    private final MailCategoryQueryRepository queryRepository;

    @CacheEvict(value = CacheConfig.CacheNames.MAIL_CATEGORIES, allEntries = true)
    public MailCategoryResponse create(MailCategoryRequest request) {
        long mailTypeId = request.mailTypeId().value();
        MailType mailType = getMailTypeOrThrow(mailTypeId);
        if (repository.existsByMailTypeIdAndCode(mailTypeId, request.code())) {
            throw new IllegalStateException(
                    "Kode \"%s\" sudah terpakai. Silahkan gunakan kode yang lain.".formatted(request.code()));
        }
        var entity = new MailCategory(mailType, request.code(), request.name());
        if (request.sort() != null) entity.setSort(request.sort());
        var saved = repository.save(entity);
        return queryRepository.findById(saved.getId()).orElseThrow();
    }

    @CacheEvict(value = CacheConfig.CacheNames.MAIL_CATEGORIES, allEntries = true)
    public MailCategoryResponse update(Long id, MailCategoryRequest request) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MailCategory not found: " + id));
        long mailTypeId = request.mailTypeId().value();
        MailType mailType = getMailTypeOrThrow(mailTypeId);

        // Check unique constraint if code or type changed
        if (!entity.getMailType().getId().equals(mailTypeId) || !entity.getCode().equals(request.code())) {
            if (repository.existsByMailTypeIdAndCode(mailTypeId, request.code())) {
                throw new IllegalStateException(
                        "Kode \"%s\" sudah terpakai. Silahkan gunakan kode yang lain.".formatted(request.code()));
            }
        }

        entity.setMailType(mailType);
        entity.setCode(request.code());
        entity.setName(request.name());
        if (request.sort() != null) entity.setSort(request.sort());
        repository.save(entity);
        return queryRepository.findById(id).orElseThrow();
    }

    @CacheEvict(value = CacheConfig.CacheNames.MAIL_CATEGORIES, allEntries = true)
    public void delete(Long id) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MailCategory not found: " + id));
        entity.markDeleted();
        repository.save(entity);
    }

    private MailType getMailTypeOrThrow(Long mailTypeId) {
        return mailTypeRepository.findById(mailTypeId)
                .orElseThrow(() -> new EntityNotFoundException("MailType not found: " + mailTypeId));
    }
}
