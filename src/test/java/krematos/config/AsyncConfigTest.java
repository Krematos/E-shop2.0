package krematos.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

// 1. Nastartuje jen lehký kontext s AsyncConfig a naší pomocnou konfigurací
@SpringJUnitConfig({AsyncConfig.class, AsyncConfigTest.TestServiceConfig.class})
class AsyncConfigTest {

    @Autowired
    private Executor taskExecutor;

    @Autowired
    private DummyAsyncService dummyAsyncService;

    @Test
    @DisplayName("Should load executor with correct configuration properties")
    void testExecutorConfiguration() {
        // Ověří, že bean je správného typu
        assertThat(taskExecutor).isInstanceOf(ThreadPoolTaskExecutor.class);

        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) taskExecutor;

        // Ověří parametry, které jsi nastavil v configu
        assertEquals(4, executor.getCorePoolSize());
        assertEquals(8, executor.getMaxPoolSize());
        assertEquals(25, executor.getQueueCapacity());
        assertEquals("AsyncExecutor-", executor.getThreadNamePrefix());
    }

    @Test
    @DisplayName("Should execute method asynchronously in a separate thread with correct prefix")
    void testAsyncExecution() throws ExecutionException, InterruptedException {
        // Získá jméno hlavního vlákna (testu)
        String mainThreadName = Thread.currentThread().getName();
        // Zavolá asynchronní metodu
        CompletableFuture<String> future = dummyAsyncService.runAsyncJob();
        // Počká na výsledek (v testu je to OK)
        String asyncThreadName = future.get();
        System.out.println("Main thread: " + mainThreadName);
        System.out.println("Async thread: " + asyncThreadName);
        // OVĚŘENÍ:
        // 1. Vlákna musí být rozdílná
        assertThat(asyncThreadName).isNotEqualTo(mainThreadName);
        // 2. Vlákno musí začínat prefixem("AsyncExecutor-")
        assertThat(asyncThreadName).startsWith("AsyncExecutor-");
    }

    // --- Pomocná třída a konfigurace pro testování funkčnosti ---

    static class DummyAsyncService {
        @Async
        public CompletableFuture<String> runAsyncJob() {
            return CompletableFuture.completedFuture(Thread.currentThread().getName());
        }
    }

    @org.springframework.boot.test.context.TestConfiguration
    static class TestServiceConfig {
        @Bean
        public DummyAsyncService dummyAsyncService() {
            return new DummyAsyncService();
        }
    }
}
