package id.perumdamts.mail.event;

import id.perumdamts.mail.dto.integration.hr.HrCacheInvalidationRequest;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class HrCacheInvalidationEvent extends ApplicationEvent {
    private final HrCacheInvalidationRequest request;

    public HrCacheInvalidationEvent(Object source, HrCacheInvalidationRequest request) {
        super(source);
        this.request = request;
    }
}
