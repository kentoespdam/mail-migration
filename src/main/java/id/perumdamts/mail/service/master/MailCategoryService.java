package id.perumdamts.mail.service.master;

import id.perumdamts.mail.api.dto.master.MailCategoryMapper;
import id.perumdamts.mail.api.dto.master.MailCategoryRequest;
import id.perumdamts.mail.api.dto.master.MailCategoryResponse;
import id.perumdamts.mail.domain.entity.MailCategory;
import id.perumdamts.mail.domain.entity.MailType;
import id.perumdamts.mail.repository.jpa.MailCategoryRepository;
import id.perumdamts.mail.repository.jpa.MailTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class MailCategoryService {

    private final MailCategoryRepository repository;
    private final MailTypeRepository mailTypeRepository;
    private final MailCategoryMapper mapper;

    public List<MailCategoryResponse> findAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    public List<MailCategoryResponse> findByMailTypeId(Integer mailTypeId) {
        return repository.findByMailTypeIdOrderBySortAsc(mailTypeId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public MailCategoryResponse findById(Integer id) {
        return mapper.toResponse(getOrThrow(id));
    }

    @Transactional
    public MailCategoryResponse create(MailCategoryRequest request) {
        MailType mailType = getMailTypeOrThrow(request.mailTypeId());
        if (repository.existsByMailTypeIdAndCode(request.mailTypeId(), request.code())) {
            throw new IllegalArgumentException(
                    "Category with code '" + request.code() + "' already exists for mail type " + request.mailTypeId());
        }
        var entity = new MailCategory(mailType, request.code(), request.name());
        if (request.sort() != null) entity.setSort(request.sort());
        return mapper.toResponse(repository.save(entity));
    }

    @Transactional
    public MailCategoryResponse update(Integer id, MailCategoryRequest request) {
        var entity = getOrThrow(id);
        MailType mailType = getMailTypeOrThrow(request.mailTypeId());

        // Check unique constraint if code or type changed
        if (!entity.getMailType().getId().equals(request.mailTypeId()) || !entity.getCode().equals(request.code())) {
            if (repository.existsByMailTypeIdAndCode(request.mailTypeId(), request.code())) {
                throw new IllegalArgumentException(
                        "Category with code '" + request.code() + "' already exists for mail type " + request.mailTypeId());
            }
        }

        entity.setMailType(mailType);
        entity.setCode(request.code());
        entity.setName(request.name());
        if (request.sort() != null) entity.setSort(request.sort());
        return mapper.toResponse(repository.save(entity));
    }

    @Transactional
    public void delete(Integer id) {
        var entity = getOrThrow(id);
        entity.markDeleted();
        repository.save(entity);
    }

    private MailCategory getOrThrow(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MailCategory not found: " + id));
    }

    private MailType getMailTypeOrThrow(Integer mailTypeId) {
        return mailTypeRepository.findById(mailTypeId)
                .orElseThrow(() -> new EntityNotFoundException("MailType not found: " + mailTypeId));
    }
}
