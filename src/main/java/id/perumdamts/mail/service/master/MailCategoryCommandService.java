package id.perumdamts.mail.service.master;

import id.perumdamts.mail.dto.master.mailCategory.MailCategoryRequest;
import id.perumdamts.mail.dto.master.mailCategory.MailCategoryResponse;
import id.perumdamts.mail.entity.master.MailCategory;
import id.perumdamts.mail.entity.master.MailType;
import id.perumdamts.mail.repository.master.jpa.MailCategoryRepository;
import id.perumdamts.mail.repository.master.jpa.MailTypeRepository;
import id.perumdamts.mail.repository.master.jooq.MailCategoryQueryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MailCategoryCommandService {

    private final MailCategoryRepository repository;
    private final MailTypeRepository mailTypeRepository;
    private final MailCategoryQueryRepository queryRepository;

    public MailCategoryResponse create(MailCategoryRequest request) {
        MailType mailType = getMailTypeOrThrow(request.mailTypeId());
        if (repository.existsByMailTypeIdAndCode(request.mailTypeId(), request.code())) {
            throw new IllegalStateException(
                    "Kode \"%s\" sudah terpakai. Silahkan gunakan kode yang lain.".formatted(request.code()));
        }
        var entity = new MailCategory(mailType, request.code(), request.name());
        if (request.sort() != null) entity.setSort(request.sort());
        var saved = repository.save(entity);
        return queryRepository.findById(saved.getId()).orElseThrow();
    }

    public MailCategoryResponse update(Long id, MailCategoryRequest request) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MailCategory not found: " + id));
        MailType mailType = getMailTypeOrThrow(request.mailTypeId());

        // Check unique constraint if code or type changed
        if (!entity.getMailType().getId().equals(request.mailTypeId()) || !entity.getCode().equals(request.code())) {
            if (repository.existsByMailTypeIdAndCode(request.mailTypeId(), request.code())) {
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
