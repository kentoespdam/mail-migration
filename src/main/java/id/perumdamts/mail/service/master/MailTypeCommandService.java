package id.perumdamts.mail.service.master;

import id.perumdamts.mail.dto.master.mailType.MailTypeRequest;
import id.perumdamts.mail.dto.master.mailType.MailTypeResponse;
import id.perumdamts.mail.entity.master.MailType;
import id.perumdamts.mail.enums.CategoryStatus;
import id.perumdamts.mail.repository.master.jooq.MailTypeQueryRepository;
import id.perumdamts.mail.repository.master.jpa.MailCategoryRepository;
import id.perumdamts.mail.repository.master.jpa.MailTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MailTypeCommandService {

    private final MailTypeRepository repository;
    private final MailCategoryRepository categoryRepository;
    private final MailTypeQueryRepository queryRepository;

    public MailTypeResponse create(MailTypeRequest request) {
        if (repository.existsByName(request.name())) {
            throw new IllegalArgumentException("Duplikasi Jenis Surat: " + request.name());
        }
        var entity = new MailType(request.name());
        var saved = repository.save(entity);
        return queryRepository.findById(saved.getId()).orElseThrow();
    }

    public MailTypeResponse update(Long id, MailTypeRequest request) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MailType not found: " + id));
        if (repository.existsByNameAndIdNot(request.name(), id)) {
            throw new IllegalArgumentException("Duplikasi Jenis Surat: " + request.name());
        }
        entity.setName(request.name());
        repository.save(entity);
        return queryRepository.findById(id).orElseThrow();
    }

    public void delete(Long id) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MailType not found: " + id));

        long activeCategories = categoryRepository.countByMailTypeAndStatusNot(entity, CategoryStatus.DELETED);
        if (activeCategories > 0) {
            throw new IllegalStateException(
                    "Terdapat " + activeCategories + " Kategori Surat yang menginduk ke Jenis Surat ini");
        }

        entity.markDeleted();
        repository.save(entity);
    }
}
