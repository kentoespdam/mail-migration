package id.perumdamts.mail.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;

/**
 * Konfigurasi async execution untuk domain event listeners.
 *
 * <p>Virtual threads diaktifkan via {@code spring.threads.virtual.enabled=true},
 * sehingga Spring Boot otomatis menyediakan {@code SimpleAsyncTaskExecutor}
 * berbasis virtual threads untuk {@code @Async} dan {@code @Scheduled}.
 *
 * <p>Digunakan oleh {@code @TransactionalEventListener + @Async} di listener:
 * <ul>
 *   <li>{@code MailSentEventListener} — update statistik + cache evict recipients</li>
 *   <li>{@code ArchivePublishedListener} — notifikasi publikasi arsip</li>
 * </ul>
 */
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) ->
                log.error("[ASYNC ERROR] Method: {}#{} params={} — {}",
                        method.getDeclaringClass().getSimpleName(),
                        method.getName(),
                        params,
                        ex.getMessage(),
                        ex);
    }
}
