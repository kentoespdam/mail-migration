package id.perumdamts.mail.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Konfigurasi async execution untuk domain event listeners.
 *
 * <p>Digunakan oleh {@code @TransactionalEventListener + @Async} di listener:
 * <ul>
 *   <li>{@code MailSentEventListener} — update statistik + cache evict recipients</li>
 *   <li>{@code ArchivePublishedListener} — notifikasi publikasi arsip</li>
 * </ul>
 *
 * <p>Thread pool di-tune agar tidak memblokir HTTP worker thread saat
 * mengirim notifikasi pasca-commit transaksi.
 */
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    /** Ukuran core pool — sesuaikan dengan jumlah CPU server. */
    private static final int CORE_POOL_SIZE  = 4;
    /** Max pool — spike handling untuk event burst. */
    private static final int MAX_POOL_SIZE   = 16;
    /** Queue capacity sebelum task di-reject. */
    private static final int QUEUE_CAPACITY  = 100;
    /** Prefix thread name — mudah diidentifikasi di log & thread dump. */
    private static final String THREAD_PREFIX = "mail-async-";

    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix(THREAD_PREFIX);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

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
