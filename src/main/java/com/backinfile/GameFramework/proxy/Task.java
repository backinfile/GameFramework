package com.backinfile.GameFramework.proxy;

import com.backinfile.GameFramework.LogCore;
import com.backinfile.support.SysException;
import com.backinfile.support.Utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Task<T> extends CompletableFuture<T> {
    public Task() {

    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        if (!isDone()) {
            throw new SysException("不允许同步调用");
        }
        return super.get();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (!this.isDone()) {
            throw new SysException("不允许同步调用");
        }
        return super.get(timeout, unit);
    }

    @Override
    public T getNow(T valueIfAbsent) {
        if (!this.isDone()) {
            throw new SysException("不允许同步调用");
        }
        return super.getNow(valueIfAbsent);
    }

    @Override
    public T join() {
        if (!this.isDone()) {
            throw new SysException("不允许同步调用");
        }
        return super.join();
    }

    public T join(long duration, String message) {
        while (!isDone()) {
            Utils.sleep(duration);
            
            if (!Utils.isNullOrEmpty(message)) {
                LogCore.core.info(message);
            }
        }
        return this.join();
    }

    public static <T> Task<T> of(Class<T> clazz) {
        return new Task<>();
    }

    public static <T> Task<T> completedTask(T value) {
        Task<T> task = new Task<>();
        task.complete(value);
        return task;
    }

    public static Task<Void> completedTask() {
        Task<Void> task = new Task<>();
        task.complete(null);
        return task;
    }


    public static <T> Task<T> failure(String message) {
        Task<T> task = new Task<>();
        task.completeExceptionally(new SysException(message));
        return task;
    }

    public static <T> Task<T> failure(Exception e) {
        Task<T> task = new Task<>();
        task.completeExceptionally(e);
        return task;
    }
}
