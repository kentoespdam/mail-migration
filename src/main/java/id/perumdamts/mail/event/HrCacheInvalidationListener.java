package id.perumdamts.mail.event;

import id.perumdamts.mail.config.CacheConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class HrCacheInvalidationListener {
    private final CacheManager cacheManager;

    @EventListener
    public void handleHrCacheInvalidation(HrCacheInvalidationEvent event) {
        var request = event.getRequest();
        log.info("Handling HR cache invalidation: {}", request);

        if ("EMPLOYEE".equalsIgnoreCase(request.getType())) {
            var cache = cacheManager.getCache(CacheConfig.CacheNames.HR_EMPLOYEE);
            if (cache != null) {
                String cacheKey = "emp:" + request.getId();
                cache.evict(cacheKey);
                log.info("Evicted HR employee cache for key: {}", cacheKey);
            }
        }
    }
}
