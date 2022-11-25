package com.backinfile.GameFramework.core;

import com.backinfile.GameFramework.LogCore;
import com.backinfile.support.SysException;
import com.backinfile.support.func.Action0;
import com.backinfile.support.timer.TimerQueue;

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
    private long time = 0;

    // 两次执行时间的时间差距
    private long deltaTime = 0;

    // 执行频率（每秒执行几次)
    private int frequency = 33;

    private static final ThreadLocal<Port> curPort = new ThreadLocal<>();
    private final ConcurrentLinkedQueue<Action0> postActionList = new ConcurrentLinkedQueue<>();

    private final TimerQueue timerQueue = new TimerQueue(this::getTime);

    protected boolean startupOver = false; // 初始化结束

    public Port(String portId) {
        setPortId(portId);
    }

    public Port() {
    }

    public void startup() {
        startupOver = true;
        LogCore.core.info("port init over {}", getClass().getName());
    }

    public void pulsePort() {

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
        pulsePort();
        // 事件队列
        timerQueue.update();
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
        curPort.set(null);
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


    public static Port getCurrentPort() {
        return curPort.get();
    }

    void setNode(Node node) {
        this.node = node;
        terminal = new Terminal(node, this);
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
    protected void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public Node getNode() {
        return node;
    }

    public long getTime() {
        return time;
    }

    public int getFrequency() {
        return frequency;
    }

    public long getDeltaTime() {
        return deltaTime;
    }


    /**
     * 是否已经初始化完毕
     */
    public boolean isStartupOver() {
        return startupOver;
    }

    /**
     * 距离下次执行的时间
     */
    public long getDelay(TimeUnit unit) {
        return time + (1000 / frequency) - node.getTime();
    }

    public int compareTo(Delayed o) {
        Port port = (Port) o;
        return Long.compare(time + (1000 / frequency), port.time + (1000 / port.frequency));
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public TimerQueue getTimerQueue() {
        return timerQueue;
    }

    void handleRequest(Call call) {
        throw new SysException("unimplemented");
    }
}
