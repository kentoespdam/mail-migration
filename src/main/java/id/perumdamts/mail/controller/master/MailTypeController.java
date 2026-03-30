package id.perumdamts.mail.controller.master;

import id.perumdamts.mail.dto.master.MailTypeLookup;
import id.perumdamts.mail.dto.master.MailTypeParams;
import id.perumdamts.mail.dto.master.MailTypeRequest;
import id.perumdamts.mail.dto.master.MailTypeResponse;
import id.perumdamts.mail.infrastructure.sqids.SqidsHelper;
import id.perumdamts.mail.service.master.MailTypeService;
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

    private final MailTypeService service;
    private final SqidsHelper sqidsHelper;

    @GetMapping
    public PagedModel<MailTypeResponse> findAll(@ParameterObject MailTypeParams params) {
        return new PagedModel<>(service.findAll(params));
    }

    @GetMapping("/lookup")
    public List<MailTypeLookup> lookup() {
        return service.lookup();
    }

    @GetMapping("/{id}")
    public MailTypeResponse findById(@PathVariable String id) {
        return service.findById(sqidsHelper.decode(id));
    }

    @PostMapping
    public ResponseEntity<MailTypeResponse> create(@Valid @RequestBody MailTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public MailTypeResponse update(@PathVariable String id, @Valid @RequestBody MailTypeRequest request) {
        return service.update(sqidsHelper.decode(id), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(sqidsHelper.decode(id));
    }
}
