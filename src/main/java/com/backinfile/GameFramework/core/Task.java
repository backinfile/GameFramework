package com.backinfile.GameFramework.core;

import com.backinfile.GameFramework.LogCore;
import com.backinfile.support.SysException;
import com.backinfile.support.Utils;
import com.backinfile.support.func.Function0;

import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

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

    public static Task<Void> run(Function0<Task<Void>> func) {
        try {
            return func.invoke();
        } catch (Exception e) {
            LogCore.core.error("error in Task#run", e);
            return Task.failure(e);
        }
    }

    @Override
    public CompletableFuture<T> whenComplete(BiConsumer<? super T, ? super Throwable> action) {
        return super.whenComplete((r, ex) -> {
            try {
                action.accept(r, ex);
            } catch (Exception e) {
                throw new SysException("error in task", e);
            }
        });
    }

    @Override
    public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action) {
        return super.whenCompleteAsync((r, ex) -> {
            try {
                action.accept(r, ex);
            } catch (Exception e) {
                throw new SysException("error in task", e);
            }
        });
    }

    @Override
    public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action, Executor executor) {
        return super.whenCompleteAsync((r, ex) -> {
            try {
                action.accept(r, ex);
            } catch (Exception e) {
                throw new SysException("error in task", e);
            }
        }, executor);
    }

    @Override
    public <U> CompletableFuture<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn) {
        return super.thenCompose((t) -> {
            try {
                return fn.apply(t);
            } catch (Exception e) {
                throw new SysException("error in task", e);
            }
        });
    }
}
