package com.acanbiler.shopwave;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Main Spring Boot application class for ShopWave.
 * 
 * This class configures the application with:
 * - Virtual threads for better performance
 * - JPA repositories for data access
 * - Async processing capabilities
 * - Scheduling for background tasks
 * - Transaction management
 */
@SpringBootApplication
@EnableJpaRepositories
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class ShopWaveApplication {

    /**
     * Main method to start the Spring Boot application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(ShopWaveApplication.class, args);
    }

    /**
     * Configure virtual thread executor for async processing.
     * 
     * Virtual threads provide better performance for I/O-bound operations
     * and are especially beneficial for web applications with many concurrent requests.
     * 
     * @return virtual thread executor
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * Configure thread pool task executor for specific async operations.
     * 
     * This is used for operations that might benefit from a traditional thread pool
     * instead of virtual threads.
     * 
     * @return thread pool task executor
     */
    @Bean(name = "threadPoolTaskExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ShopWave-");
        executor.initialize();
        return executor;
    }
}