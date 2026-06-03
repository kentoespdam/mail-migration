package id.perumdamts.mail.controller.master;

import id.perumdamts.mail.dto.id.QuickMessageId;
import id.perumdamts.mail.dto.master.quickMessage.QuickMessageParams;
import id.perumdamts.mail.dto.master.quickMessage.QuickMessageRequest;
import id.perumdamts.mail.dto.master.quickMessage.QuickMessageResponse;
import id.perumdamts.mail.service.master.quickMessage.QuickMessageCommandService;
import id.perumdamts.mail.service.master.quickMessage.QuickMessageQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quick-messages")
@RequiredArgsConstructor
public class QuickMessageController {

    private final QuickMessageCommandService commandService;
    private final QuickMessageQueryService queryService;

    @GetMapping("/lookup")
    public List<QuickMessageResponse> lookup() {
        return queryService.lookup();
    }

    @GetMapping
    public PagedModel<QuickMessageResponse> findAll(@ParameterObject QuickMessageParams params) {
        return new PagedModel<>(queryService.findAll(params));
    }

    @GetMapping("/{id}")
    public QuickMessageResponse findById(@PathVariable QuickMessageId id) {
        return queryService.findById(id.value());
    }

    @PostMapping
    public ResponseEntity<QuickMessageResponse> create(@Valid @RequestBody QuickMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commandService.create(request));
    }

    @PutMapping("/{id}")
    public QuickMessageResponse update(@PathVariable QuickMessageId id, @Valid @RequestBody QuickMessageRequest request) {
        return commandService.update(id.value(), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable QuickMessageId id) {
        commandService.delete(id.value());
    }

    @PatchMapping("/{id}/status")
    public QuickMessageResponse toggleStatus(@PathVariable QuickMessageId id) {
        return commandService.toggleStatus(id.value());
    }
}
