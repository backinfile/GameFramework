package com.backinfile.GameFramework.service;

import com.backinfile.GameFramework.core.Node;
import com.backinfile.GameFramework.core.Service;
import com.backinfile.GameFramework.core.Task;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 代替主线程发送rpc
 * 此Service接受到的rpc会转发到主线程
 * 可以使用 {@link Node#addMainThreadPort()} 添加到Node上
 * 在主线程发起rpc时, 使用Service1Proxy.createInstance(true)由本service代发
 */
public class MainThreadService extends Service {
    private final Queue<Task<Void>> waitingTasks = new ConcurrentLinkedQueue<>();

    @Override
    public void init() {

    }

    @Override
    public void pulse() {

    }

    @Override
    public void pulsePerSec() {

    }

    // 等待主线程主动更新
    public Task<Void> waitMainThreadUpdate() {
        Task<Void> waitTask = new Task<>();
        waitingTasks.add(waitTask);
        return waitTask;
    }

    // 主线程主动更新
    public void mainThreadUpdate() {
        while (!waitingTasks.isEmpty()) {
            Task<Void> task = waitingTasks.poll();
            if (task != null) {
                task.complete(null);
            }
        }
    }
}
