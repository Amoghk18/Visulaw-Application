package com.visulaw.legal_service.application.service.async;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface AsyncExecutorService {

    public void executeAsync(Runnable task);

    public <T> Future<T> submitCallable(Callable<T> task);

}
