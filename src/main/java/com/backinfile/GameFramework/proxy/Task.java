package com.backinfile.GameFramework.proxy;

import com.backinfile.support.SysException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Task<T> extends CompletableFuture<T> {
    private boolean succeed = false;

    public Task() {

    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        if (!succeed) {
            throw new SysException("不允许同步调用");
        }
        return super.get();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (!this.succeed) {
            throw new SysException("不允许同步调用");
        }
        return super.get(timeout, unit);
    }

    @Override
    public T getNow(T valueIfAbsent) {
        if (!this.succeed) {
            throw new SysException("不允许同步调用");
        }
        return super.getNow(valueIfAbsent);
    }

    @Override
    public T join() {
        if (!this.succeed) {
            throw new SysException("不允许同步调用");
        }
        return super.join();
    }

    @Override
    public boolean complete(T value) {
        this.succeed = true;
        return super.complete(value);
    }

    @Override
    public boolean completeExceptionally(Throwable ex) {
        this.succeed = true;
        return super.completeExceptionally(ex);
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
