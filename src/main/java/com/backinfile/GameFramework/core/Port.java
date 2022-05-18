package com.backinfile.GameFramework.core;

import com.backinfile.GameFramework.LogCore;
import com.backinfile.GameFramework.proxy.AsyncObject;
import com.backinfile.GameFramework.proxy.ProxyManager;
import com.backinfile.support.func.Action0;
import com.backinfile.support.timer.TimerQueue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public abstract class Port implements Delayed {

    private Node node;

    private String portId;

    private Terminal terminal;

    /**
     * 用于检查是否需要心跳操作，（在额外的唤醒中不执行心跳）
     */
    public volatile boolean pulsed = false;

    // 上次执行时间
    protected long time = 0;

    // 两次执行时间的时间差距
    private long deltaTime = 0;

    // 执行频率（每秒执行几次)
    private int HZ = 33;

    private static final ThreadLocal<Port> curPort = new ThreadLocal<>();
    private final ConcurrentLinkedQueue<Action0> postActionList = new ConcurrentLinkedQueue<>();

    private final Map<Long, AsyncObject> asyncObjectMap = new HashMap<>();
    protected TimerQueue timerQueue = new TimerQueue(this::getTime);

    protected boolean startupOver = false; // 初始化结束

    public Port(String portId) {
        this.portId = portId;
    }

    public static Port getCurrentPort() {
        return curPort.get();
    }

    void setNode(Node node) {
        this.node = node;
        terminal = new Terminal(node, this);
    }

    public void startup() {
        startupOver = true;
    }

    public void pulse() {

    }

    public void caseRunOnce() {
        // 设置port时间
        long newTime = node.getTime();
        this.deltaTime = time > 0 ? newTime - time : 0;
        this.time = newTime;
        // 设置当前port
        curPort.set(this);
        terminal.pulse();
        // 心跳
        pulse();
        timerQueue.update();
        if (startupOver) {
            for (AsyncObject obj : asyncObjectMap.values()) {
                obj.casePulse();
            }
        }
        // 执行post函数
        while (!postActionList.isEmpty()) {
            Action0 action = postActionList.poll();
            try {
                action.invoke();
            } catch (Exception e) {
                LogCore.core.error("error in post action", e);
            }
        }
        // 设置当前port
        curPort.set(null);
    }

    /**
     * 临时唤醒来执行rpc调用
     */
    public void caseAwakeUp() {
        curPort.set(this);
        terminal.executeInCall();
        // 执行post函数
        while (!postActionList.isEmpty()) {
            Action0 action = postActionList.poll();
            try {
                action.invoke();
            } catch (Exception e) {
                LogCore.core.error("error in post action", e);
            }
        }
        curPort.set(null);
    }

    public String getPortId() {
        return portId;
    }

    public void setPortId(String portId) {
        this.portId = portId;
    }

    /**
     * 设置每秒执行次数
     */
    protected void setHZ(int HZ) {
        this.HZ = HZ;
    }

    public Node getNode() {
        return node;
    }

    public long getTime() {
        if (time > 0)
            return time;
        return System.currentTimeMillis();
    }

    public long getDeltaTime() {
        return deltaTime;
    }

    /**
     * 将一个函数推迟到心跳结束执行
     */
    public void post(Action0 action) {
        postActionList.add(action);
        if (node != null) {
            node.awake(this);
        }
    }

    public boolean isStartupOver() {
        return startupOver;
    }

    /**
     * 距离下次执行的时间
     */
    public long getDelay(TimeUnit unit) {
        return time + (1000 / HZ) - node.getTime();
    }

    public int compareTo(Delayed o) {
        Port port = (Port) o;
        return Long.compare(time + (1000 / HZ), port.time + (1000 / port.HZ));
    }

    public Terminal getTerminal() {
        return terminal;
    }

    // 线程安全
    public void add(AsyncObject obj) {
        this.post(() -> {
            addAsyncObj(obj);
        });
    }

    // 线程安全
    public void remove(long id) {
        this.post(() -> {
            removeAsyncObj(id);
        });
    }

    // 非线程安全
    protected void addAsyncObj(AsyncObject obj) {
        ProxyManager.registerPortId(obj.getClass(), getPortId());

        asyncObjectMap.put(obj.getObjId(), obj);
        obj.onAttach(this);
    }

    // 非线程安全
    protected AsyncObject removeAsyncObj(long id) {
        AsyncObject asyncObject = asyncObjectMap.get(id);
        if (asyncObject != null) {
            asyncObject.onDetach(this);
        }
        return asyncObjectMap.remove(id);
    }

    // 非线程安全
    public AsyncObject getAsyncObj(long id) {
        return asyncObjectMap.get(id);
    }


    public static Port of(AsyncObject asyncObject) {
        return new Port(asyncObject.getClass().getSimpleName() + "Port") {
            @Override
            public void startup() {
                add(asyncObject);
                super.startup();
            }
        };
    }
}
