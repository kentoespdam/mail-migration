package id.perumdamts.mail.service.master;

import id.perumdamts.mail.dto.master.messagetemplate.MessageTemplateMapper;
import id.perumdamts.mail.dto.master.messagetemplate.MessageTemplateRequest;
import id.perumdamts.mail.dto.master.messagetemplate.MessageTemplateResponse;
import id.perumdamts.mail.entity.master.MessageTemplate;
import id.perumdamts.mail.repository.master.jpa.MessageTemplateRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MessageTemplateService {

    private final MessageTemplateRepository repository;
    private final MessageTemplateMapper mapper;

    public MessageTemplateService(MessageTemplateRepository repository,
                                  MessageTemplateMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<MessageTemplateResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<MessageTemplateResponse> findAllList() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MessageTemplateResponse findById(Long id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("MessageTemplate not found"));
    }

    public MessageTemplateResponse create(MessageTemplateRequest request) {
        MessageTemplate entity = mapper.toEntity(request);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    public MessageTemplateResponse update(Long id, MessageTemplateRequest request) {
        MessageTemplate entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MessageTemplate not found"));
        mapper.updateEntity(entity, request);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
