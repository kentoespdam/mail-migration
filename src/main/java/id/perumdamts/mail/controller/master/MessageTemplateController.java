package id.perumdamts.mail.controller.master;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import id.perumdamts.mail.dto.id.MessageTemplateId;
import id.perumdamts.mail.dto.master.messagetemplate.MessageTemplateRequest;
import id.perumdamts.mail.dto.master.messagetemplate.MessageTemplateResponse;
import id.perumdamts.mail.service.master.MessageTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/master/message-templates")
@Tag(name = "Message Template", description = "Master data Message Template")
public class MessageTemplateController {

    private final MessageTemplateService service;

    public MessageTemplateController(MessageTemplateService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Get all message templates (paginated)")
    public PagedModel<MessageTemplateResponse> findAll(Pageable pageable) {
        return new PagedModel<>(service.findAll(pageable));
    }

    @GetMapping("/list")
    @Operation(summary = "Get all message templates (list)")
    public List<MessageTemplateResponse> findAllList() {
        return service.findAllList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get message template by id")
    public MessageTemplateResponse findById(@PathVariable MessageTemplateId id) {
        return service.findById(id.value());
    }

    @PostMapping
    @Operation(summary = "Create message template")
    public ResponseEntity<MessageTemplateResponse> create(@Valid @RequestBody MessageTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update message template")
    public MessageTemplateResponse update(@PathVariable MessageTemplateId id, @Valid @RequestBody MessageTemplateRequest request) {
        return service.update(id.value(), request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete message template")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable MessageTemplateId id) {
        service.delete(id.value());
    }
}
