package id.perumdamts.mail.service.master;

import id.perumdamts.mail.api.dto.master.MailTypeMapper;
import id.perumdamts.mail.api.dto.master.MailTypeRequest;
import id.perumdamts.mail.api.dto.master.MailTypeResponse;
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
public class MailTypeService {

    private final MailTypeRepository repository;
    private final MailCategoryRepository categoryRepository;
    private final MailTypeMapper mapper;

    public List<MailTypeResponse> findAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    public MailTypeResponse findById(Integer id) {
        return mapper.toResponse(getOrThrow(id));
    }

    @Transactional
    public MailTypeResponse create(MailTypeRequest request) {
        if (repository.existsByName(request.name())) {
            throw new IllegalArgumentException("Mail type with name '" + request.name() + "' already exists");
        }
        var entity = new MailType(request.name());
        return mapper.toResponse(repository.save(entity));
    }

    @Transactional
    public MailTypeResponse update(Integer id, MailTypeRequest request) {
        var entity = getOrThrow(id);
        repository.findByName(request.name())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(_ -> {
                    throw new IllegalArgumentException("Mail type with name '" + request.name() + "' already exists");
                });
        entity.setName(request.name());
        return mapper.toResponse(repository.save(entity));
    }

    @Transactional
    public void delete(Integer id) {
        var entity = getOrThrow(id);
        if (categoryRepository.existsByMailTypeId(id)) {
            throw new IllegalStateException("Cannot delete MailType " + id + ": categories still reference it");
        }
        entity.markDeleted();
        repository.save(entity);
    }

    private MailType getOrThrow(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MailType not found: " + id));
    }
}
