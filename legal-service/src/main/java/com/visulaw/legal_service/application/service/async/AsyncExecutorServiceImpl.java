package com.visulaw.legal_service.application.service.async;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncExecutorServiceImpl implements AsyncExecutorService {

    private final ExecutorService executorService;

    @Override
    public void executeAsync(Runnable task) {
        executorService.submit(() -> withRetries(task, 3));
    }

    @Override
    public <T> Future<T> submitCallable(Callable<T> task) {
        return executorService.submit(task);
    }

    private void withRetries(Runnable task, int maxRetries) {
        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                task.run();
                return;
            } catch (Exception e) {
                attempt++;
                log.error("Async task failed on attempt {}/{}", attempt, maxRetries, e);
                try {
                    Thread.sleep(1000L * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
        log.error("Async task failed after {} attempts", maxRetries);
    }
}
