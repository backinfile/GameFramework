package com.backinfile.GameFramework.core;

import com.backinfile.GameFramework.LogCore;
import com.backinfile.GameFramework.serialize.SerializableManager;
import com.backinfile.support.SysException;
import com.backinfile.support.Utils;
import com.backinfile.support.func.Action0;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.DelayQueue;

/**
 * 管理Port，连接远程Node
 * 一个程序只启一个Node
 */
public class Node {
    public static Node Instance = null;
    public static final Thread MainThread = Thread.currentThread(); // TODO 主线程也可发送rpc

    private final ConcurrentLinkedQueue<Port> portsWaitForRun = new ConcurrentLinkedQueue<>();
    private final DelayQueue<Port> portsWaitForReschedule = new DelayQueue<>();
    private final ConcurrentHashMap<String, Port> allPorts = new ConcurrentHashMap<>();
    private DispatchThreads dispatchThreads;
    private DispatchThreads mainThread;
    private static final int THREAD_NUM = 3;
    private final ConcurrentLinkedQueue<Action0> postActionList = new ConcurrentLinkedQueue<>();
    private final String nodeId;

    public Node() {
        this("MainNode");
    }

    public Node(String nodeId) {
        this.nodeId = nodeId;
        Instance = this;
    }

    public void startUp() {
        String name = nodeId;
        Thread.currentThread().setName("Node-" + name);
        LogCore.core.info("=============== node {} 启动 ===============", nodeId);

        dispatchThreads = new DispatchThreads(("Node-" + name) + "-DispatchThread", THREAD_NUM,
                null, this::dispatchRun, null);
        dispatchThreads.start();

        mainThread = new DispatchThreads(("Node-" + name) + "-MainDispatchThread", 1, null, this::pulse, null);
        mainThread.start();

    }


    public void abort() {
        LogCore.core.info("node {} 中断开始.....", nodeId);
        dispatchThreads.abort();
        mainThread.abort();
    }

    public void join() {
        while (!dispatchThreads.isAborted() || !mainThread.isAborted()) {
            Utils.sleep(100);
        }
        LogCore.core.info("=============== node {} 关闭 ===============", nodeId);
    }

    public void waitAllPortStartupFinish() {
        while (!allPorts.values().stream().allMatch(port -> port.startupOver)) {
            Utils.sleep(1000);
            LogCore.core.info("waiting all port startup...");
        }
    }

    public void addPort(Port... ports) {
        for (Port port : ports) {
            allPorts.put(port.getPortId(), port);
            port.setNode(this);
        }

        this.post(() -> {
            for (Port port : ports) {
                LogCore.core.info("add port {} of class {}", port.getPortId(), port.getClass().getName());
                port.startup();
                portsWaitForRun.add(port);
            }
        });
    }

    private void dispatchRun() {
        // pulse port
        Port port = portsWaitForRun.poll();
        if (port == null) {
            reSchedule(THREAD_NUM);
            Utils.sleep(1);
        } else {
            pulsePort(port);
        }
    }

    private void pulse() {
        // pulse post action
        while (true) {
            Action0 action0 = postActionList.poll();
            if (action0 == null) {
                break;
            }
            try {
                action0.invoke();
            } catch (Exception e) {
                LogCore.core.error("error in invoke postAction", e);
            }
        }
    }

    private void pulsePort(Port port) {
        if (!port.pulsed) {
            port.caseRunOnce();
            port.pulsed = true;
        } else {
            port.caseAwakeUp();
        }
        portsWaitForReschedule.add(port);
    }

    // 立即唤醒一个port
    public void awake(Port port) {
        if (portsWaitForReschedule.remove(port)) {
            portsWaitForRun.add(port);
        }
    }

    // 将已经被执行过的port重新放入执行队列
    private void reSchedule(int num) {
        for (int i = 0; i < num; i++) {
            Port port = portsWaitForReschedule.poll();
            if (port == null) {
                break;
            }
            port.pulsed = false;
            portsWaitForRun.add(port);
        }
    }

    public long getTime() {
        return System.currentTimeMillis();
    }

    public Port getPort(String portId) {
        return allPorts.get(portId);
    }

    /**
     * 转发所有经由此node的call
     * 如果call发送至此node，直接推送到相应port中；
     * 此方法线程安全
     */
    public void handleCall(Call call) {
        // 发送到此node的消息
        Port port = getPort(call.to.portID);
        if (port == null) {
            LogCore.core.error("此call发送到未知port(" + call.to.portID + ")，已忽略", new SysException(""));
            return;
        }
        port.getTerminal().addCall(SerializableManager.clone(call));
        awake(port);
    }

    /**
     * 在node线程中执行action
     */
    public void post(Action0 action0) {
        postActionList.add(action0);
    }

    public String getId() {
        return nodeId;
    }

}
