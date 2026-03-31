package id.perumdamts.mail.controller.master;

import id.perumdamts.mail.dto.master.documentType.DocumentTypeDto;
import id.perumdamts.mail.dto.core.publication.PublicationMapper;
import id.perumdamts.mail.dto.master.documentType.DocumentTypeParams;
import id.perumdamts.mail.repository.master.jpa.DocumentTypeRepository;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public PagedModel<DocumentTypeDto> list(@ParameterObject DocumentTypeParams params) {
        return new PagedModel<>(repository.findAll(params.toSpecification(), params.toPageable()).map(mapper::toDto));
    }
}
