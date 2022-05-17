package com.backinfile.GameFramework.core;

import com.backinfile.GameFramework.Log;
import com.backinfile.support.Utils;
import com.backinfile.support.func.Action0;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 开启若干个线程来执行一个Action0
 */
public class DispatchThreads {
    private final String name;
    private final Action0 start;
    private final Action0 run;
    private final Action0 dispose;
    private final int num;

    private final ArrayList<Thread> threads = new ArrayList<>();
    private volatile boolean threadAbort = false;
    private final AtomicInteger abortedNum = new AtomicInteger(0);

    public DispatchThreads(Action0 run) {
        this(3, run);
    }


    public DispatchThreads(int num, Action0 run) {
        this("DispatchThread", num, null, run, null);
    }

    public DispatchThreads(String name, int num, Action0 start, Action0 run, Action0 dispose) {
        this.name = name;
        this.num = num;
        this.start = start;
        this.run = run;
        this.dispose = dispose;
    }


    public void start() {
        for (int i = 0; i < num; i++) {
            Thread thread = new Thread(this::runThread);
            thread.setName(name + "-" + i);
            thread.setDaemon(true);
            threads.add(thread);
            thread.start();
            Log.game.info("Thread {} start", thread.getName());
        }
    }

    private void runThread() {
        if (start != null) {
            try {
                start.invoke();
            } catch (Exception e) {
                Log.core.error("线程运行start出错", e);
            }
        }
        while (!threadAbort) {
            try {
                run.invoke();
            } catch (Exception e) {
                Log.core.error("线程运行run出错", e);
            }
        }
        if (dispose != null) {
            try {
                dispose.invoke();
            } catch (Exception e) {
                Log.core.error("线程运行dispose出错", e);
            }
        }
        abortedNum.incrementAndGet();
        Log.game.info("Thread {} finish", Thread.currentThread().getName());
    }

    public void abort() {
        threadAbort = true;
    }

    public void abortSync() {
        threadAbort = true;
        while (!isAborted()) {
            Utils.sleep(1);
        }
    }

    public final boolean isAborted() {
        return abortedNum.get() >= num;
    }
}
