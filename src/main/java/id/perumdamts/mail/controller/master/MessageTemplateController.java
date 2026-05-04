package id.perumdamts.mail.controller.master;

import id.perumdamts.mail.dto.master.messagetemplate.MessageTemplateRequest;
import id.perumdamts.mail.dto.master.messagetemplate.MessageTemplateResponse;
import id.perumdamts.mail.service.master.MessageTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.web.PagedModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public MessageTemplateResponse findById(@PathVariable String id) {
        return service.findById(id);
    }

    @PostMapping
    @Operation(summary = "Create message template")
    public ResponseEntity<MessageTemplateResponse> create(@Valid @RequestBody MessageTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update message template")
    public MessageTemplateResponse update(@PathVariable String id, @Valid @RequestBody MessageTemplateRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete message template")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
