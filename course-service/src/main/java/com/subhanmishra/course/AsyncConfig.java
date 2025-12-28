package com.subhanmishra.course;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class AsyncConfig {

    @Bean(destroyMethod = "shutdown")
    public ExecutorService orderProcessingExecutor() {
        // Pool size arbitrary; enough to create overlap between poll/commit and processing
        return Executors.newFixedThreadPool(8);
        // One virtual thread per task; cheap to create, no fixed pool size
//        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
