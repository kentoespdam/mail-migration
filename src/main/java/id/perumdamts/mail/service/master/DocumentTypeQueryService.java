package id.perumdamts.mail.service.master;

import id.perumdamts.mail.dto.master.documentType.DocumentTypeLookup;
import id.perumdamts.mail.dto.master.documentType.DocumentTypeMapper;
import id.perumdamts.mail.dto.master.documentType.DocumentTypeParams;
import id.perumdamts.mail.dto.master.documentType.DocumentTypeResponse;
import id.perumdamts.mail.enums.RecordStatus;
import id.perumdamts.mail.repository.master.jpa.DocumentTypeRepository;
import id.perumdamts.mail.repository.master.jooq.DocumentTypeQueryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DocumentTypeQueryService {

    private final DocumentTypeQueryRepository repository;
    private final DocumentTypeRepository jpaRepository; // For lookup if needed, or I can use jOOQ
    private final DocumentTypeMapper mapper;

    public Page<DocumentTypeResponse> findAll(DocumentTypeParams params) {
        return repository.findAll(params);
    }

    public List<DocumentTypeLookup> lookup() {
        return jpaRepository.findAllByStatusOrderByIdAsc(RecordStatus.ACTIVE).stream()
                .map(mapper::toLookup)
                .toList();
    }

    public DocumentTypeResponse findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("DocumentType not found: " + id));
    }
}
