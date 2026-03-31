package id.perumdamts.mail.controller.master;

import id.perumdamts.mail.dto.master.quickMessage.QuickMessageParams;
import id.perumdamts.mail.dto.master.quickMessage.QuickMessageRequest;
import id.perumdamts.mail.dto.master.quickMessage.QuickMessageResponse;
import id.perumdamts.mail.entity.master.QuickMessage;
import id.perumdamts.mail.service.master.QuickMessageService;
import id.perumdamts.mail.util.SqidsEncoder;
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
    private final QuickMessageService service;
    private final SqidsEncoder encoder;

    @GetMapping("/lookup")
    public List<QuickMessageResponse> lookup() {
        return service.lookup();
    }

    @GetMapping
    public PagedModel<QuickMessageResponse> findAll(@ParameterObject QuickMessageParams params) {
        return new PagedModel<>(service.findAll(params));
    }

    @GetMapping("/{id}")
    public QuickMessageResponse findById(@PathVariable String id) {
        long rawId = encoder.decode(QuickMessage.class, id);
        return service.findById(rawId);
    }

    @PostMapping
    public ResponseEntity<QuickMessageResponse> create(@Valid @RequestBody QuickMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public QuickMessageResponse update(@PathVariable String id,
                                        @Valid @RequestBody QuickMessageRequest request) {
        long rawId = encoder.decode(QuickMessage.class, id);
        return service.update(rawId, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        long rawId = encoder.decode(QuickMessage.class, id);
        service.delete(rawId);
    }
}
