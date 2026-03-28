package id.perumdamts.mail.api.rest;

import id.perumdamts.mail.api.dto.publication.DocumentTypeDto;
import id.perumdamts.mail.api.dto.publication.PublicationMapper;
import id.perumdamts.mail.repository.jpa.DocumentTypeRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/document-types")
public class DocumentTypeController {

    private final DocumentTypeRepository repository;
    private final PublicationMapper mapper;

    public DocumentTypeController(DocumentTypeRepository repository, PublicationMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @GetMapping
    public List<DocumentTypeDto> list() {
        return repository.findByStatus(1).stream()
                .map(mapper::toDto)
                .toList();
    }
}
