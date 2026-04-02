package id.perumdamts.mail.service.master;

import id.perumdamts.mail.dto.master.documentType.DocumentTypeRequest;
import id.perumdamts.mail.dto.master.documentType.DocumentTypeResponse;
import id.perumdamts.mail.entity.master.DocumentType;
import id.perumdamts.mail.repository.master.jooq.DocumentTypeQueryRepository;
import id.perumdamts.mail.repository.master.jpa.DocumentTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class DocumentTypeCommandService {

    private final DocumentTypeRepository repository;
    private final DocumentTypeQueryRepository queryRepository; // To return Response after create/update

    public DocumentTypeResponse create(DocumentTypeRequest request) {
        if (repository.existsByName(request.name())) {
            throw new IllegalArgumentException("Duplikasi Jenis Dokumen: " + request.name());
        }
        var entity = new DocumentType(request.name());
        var saved = repository.save(entity);
        return queryRepository.findById(saved.getId()).orElseThrow();
    }

    public DocumentTypeResponse update(Long id, DocumentTypeRequest request) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("DocumentType not found: " + id));

        if (repository.existsByNameAndIdNot(request.name(), id)) {
            throw new IllegalArgumentException("Duplikasi Jenis Dokumen: " + request.name());
        }

        entity.setName(request.name());
        repository.save(entity);
        return queryRepository.findById(id).orElseThrow();
    }

    public void delete(Long id) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("DocumentType not found: " + id));

        long publicationCount = entity.getPublications() != null ? entity.getPublications().size() : 0;
        if (publicationCount > 0) {
            throw new IllegalStateException("Terdapat " + publicationCount + " Publikasi yang menggunakan Jenis Dokumen ini");
        }

        entity.markDeleted();
        repository.save(entity);
    }
}
