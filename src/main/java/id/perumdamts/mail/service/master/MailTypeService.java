package id.perumdamts.mail.service.master;

import id.perumdamts.mail.dto.master.MailTypeLookup;
import id.perumdamts.mail.dto.master.MailTypeMapper;
import id.perumdamts.mail.dto.master.MailTypeRequest;
import id.perumdamts.mail.dto.master.MailTypeResponse;
import id.perumdamts.mail.entity.master.MailType;
import id.perumdamts.mail.enums.CategoryStatus;
import id.perumdamts.mail.enums.RecordStatus;
import id.perumdamts.mail.repository.master.jpa.MailCategoryRepository;
import id.perumdamts.mail.repository.master.jpa.MailTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class MailTypeService {

    private final MailTypeRepository repository;
    private final MailCategoryRepository categoryRepository;
    private final MailTypeMapper mapper;

    public Page<MailTypeResponse> findAll(String search, Pageable pageable) {
        Specification<MailType> spec = Specification.where(nameLike(search));
        return repository.findAll(spec, pageable).map(mapper::toResponse);
    }

    public List<MailTypeLookup> lookup() {
        return repository.findAllByStatusOrderByIdAsc(RecordStatus.ACTIVE).stream()
                .map(mapper::toLookup)
                .toList();
    }

    public MailTypeResponse findById(Integer id) {
        return mapper.toResponse(getOrThrow(id));
    }

    @Transactional
    public MailTypeResponse create(MailTypeRequest request) {
        if (repository.existsByName(request.name())) {
            throw new IllegalArgumentException("Duplikasi Jenis Surat: " + request.name());
        }
        var entity = new MailType(request.name());
        return mapper.toResponse(repository.save(entity));
    }

    @Transactional
    public MailTypeResponse update(Integer id, MailTypeRequest request) {
        var entity = getOrThrow(id);
        if (repository.existsByNameAndIdNot(request.name(), id)) {
            throw new IllegalArgumentException("Duplikasi Jenis Surat: " + request.name());
        }
        entity.setName(request.name());
        return mapper.toResponse(repository.save(entity));
    }

    @Transactional
    public void delete(Integer id) {
        var entity = getOrThrow(id);

        long activeCategories = categoryRepository.countByMailTypeAndStatusNot(entity, CategoryStatus.DELETED);
        if (activeCategories > 0) {
            throw new IllegalStateException(
                    "Terdapat " + activeCategories + " Kategori Surat yang menginduk ke Jenis Surat ini");
        }

        entity.markDeleted();
        repository.save(entity);
    }

    private MailType getOrThrow(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MailType not found: " + id));
    }

    private static Specification<MailType> nameLike(String keyword) {
        return (root, query, cb) ->
                keyword == null || keyword.isBlank()
                        ? null
                        : cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%");
    }
}
