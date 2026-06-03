package id.perumdamts.mail.controller.integration;

import id.perumdamts.mail.dto.integration.hr.HrCacheInvalidationRequest;
import id.perumdamts.mail.event.HrCacheInvalidationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/hr/cache")
@RequiredArgsConstructor
@Slf4j
public class HrCacheController {
    private final ApplicationEventPublisher eventPublisher;

    @PostMapping("/invalidate")
    public ResponseEntity<Void> invalidate(@RequestBody HrCacheInvalidationRequest request) {
        log.info("Received HR cache invalidation request: {}", request);
        eventPublisher.publishEvent(new HrCacheInvalidationEvent(this, request));
        return ResponseEntity.ok().build();
    }
}
