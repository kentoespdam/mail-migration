package id.perumdamts.mail.service.master;

import id.perumdamts.mail.dto.master.documentType.*;
import id.perumdamts.mail.entity.master.DocumentType;
import id.perumdamts.mail.enums.RecordStatus;
import id.perumdamts.mail.repository.master.jpa.DocumentTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class DocumentTypeService {

    private final DocumentTypeRepository repository;
    private final DocumentTypeMapper mapper;

    public Page<DocumentTypeResponse> findAll(DocumentTypeParams params) {
        return repository.findAll(params.toSpecification(), params.toPageable()).map(mapper::toResponse);
    }

    public List<DocumentTypeLookup> lookup() {
        return repository.findAllByStatusOrderByIdAsc(RecordStatus.ACTIVE).stream()
                .map(mapper::toLookup)
                .toList();
    }

    public DocumentTypeResponse findById(Long id) {
        return mapper.toResponse(getOrThrow(id));
    }

    @Transactional
    public DocumentTypeResponse create(DocumentTypeRequest request) {
        if (repository.existsByName(request.name())) {
            throw new IllegalArgumentException("Duplikasi Jenis Dokumen: " + request.name());
        }
        var entity = new DocumentType(request.name());
        return mapper.toResponse(repository.save(entity));
    }

    @Transactional
    public DocumentTypeResponse update(Long id, DocumentTypeRequest request) {
        var entity = getOrThrow(id);
        if (repository.existsByNameAndIdNot(request.name(), id)) {
            throw new IllegalArgumentException("Duplikasi Jenis Dokumen: " + request.name());
        }
        entity.setName(request.name());
        return mapper.toResponse(repository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        var entity = getOrThrow(id);

        long publicationCount = entity.getPublications() != null ? entity.getPublications().size() : 0;
        if (publicationCount > 0) {
            throw new IllegalStateException("Terdapat " + publicationCount + " Publikasi yang menggunakan Jenis Dokumen ini");
        }

        entity.markDeleted();
        repository.save(entity);
    }

    private DocumentType getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("DocumentType not found: " + id));
    }
}
