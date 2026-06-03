package id.perumdamts.mail.controller.master;

import id.perumdamts.mail.dto.id.MailTypeId;
import id.perumdamts.mail.dto.master.mailType.MailTypeLookup;
import id.perumdamts.mail.dto.master.mailType.MailTypeParams;
import id.perumdamts.mail.dto.master.mailType.MailTypeRequest;
import id.perumdamts.mail.dto.master.mailType.MailTypeResponse;
import id.perumdamts.mail.service.master.mailType.MailTypeCommandService;
import id.perumdamts.mail.service.master.mailType.MailTypeQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mail-types")
@RequiredArgsConstructor
public class MailTypeController {

    private final MailTypeCommandService commandService;
    private final MailTypeQueryService queryService;

    @GetMapping
    public PagedModel<MailTypeResponse> findAll(@ParameterObject MailTypeParams params) {
        return new PagedModel<>(queryService.findAll(params));
    }

    @GetMapping("/lookup")
    public List<MailTypeLookup> lookup() {
        return queryService.lookup();
    }

    @GetMapping("/{id}")
    public MailTypeResponse findById(@PathVariable MailTypeId id) {
        return queryService.findById(id.value());
    }

    @PostMapping
    public ResponseEntity<MailTypeResponse> create(@Valid @RequestBody MailTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commandService.create(request));
    }

    @PutMapping("/{id}")
    public MailTypeResponse update(@PathVariable MailTypeId id, @Valid @RequestBody MailTypeRequest request) {
        return commandService.update(id.value(), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable MailTypeId id) {
        commandService.delete(id.value());
    }

    @PatchMapping("/{id}/status")
    public MailTypeResponse toggleStatus(@PathVariable MailTypeId id) {
        return commandService.toggleStatus(id.value());
    }
}
