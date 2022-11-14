package com.backinfile.GameFramework.core;

import com.backinfile.GameFramework.LogCore;
import com.backinfile.GameFramework.serialize.SerializableManager;
import com.backinfile.GameFramework.service.MainThreadService;
import com.backinfile.support.Time;
import com.backinfile.support.Utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.DelayQueue;

/**
 * 管理Port，连接远程Node
 * 一个程序只启一个Node
 */
public class Node {
    private static Node instance = null;

    private final ConcurrentLinkedQueue<Port> portsWaitForRun = new ConcurrentLinkedQueue<>();
    private final DelayQueue<Port> portsWaitForReschedule = new DelayQueue<>();
    private final ConcurrentHashMap<String, Port> allPorts = new ConcurrentHashMap<>();
    private DispatchThreads dispatchThreads;
    private static final int THREAD_NUM = 3;
    private final String nodeId;
    private MainThreadService mainThreadService = null;

    public Node() {
        this("MainNode");
    }

    public Node(String nodeId) {
        this.nodeId = nodeId;
        instance = this;
    }

    public static Node getInstance() {
        return instance;
    }

    public void startUp() {
        String name = nodeId;
        Thread.currentThread().setName("Node-" + name);
        LogCore.core.info("=============== node {} 启动 ===============", nodeId);

        dispatchThreads = new DispatchThreads(("Node-" + name) + "-DispatchThread", THREAD_NUM,
                null, this::dispatchRun, null);
        dispatchThreads.start();

    }


    public void abort() {
        LogCore.core.info("=============== node {} 关闭中 ===============", nodeId);
        dispatchThreads.abort();
    }

    public void join() {
        while (isAlive()) {
            Utils.sleep(100);
        }
        LogCore.core.info("=============== node {} 已关闭 ===============", nodeId);
    }

    public boolean isAlive() {
        return !dispatchThreads.isAborted();
    }

    public void waitAllPortStartupFinish() {
        Utils.sleep(Time.SEC);
        while (!allPorts.values().stream().allMatch(Port::isStartupOver)) {
            Utils.sleep(Time.SEC);
            LogCore.core.info("waiting all port startup...");
        }
        LogCore.core.info("all port startup finished");
    }

    public void addPort(Port... ports) {
        for (Port port : ports) {
            allPorts.put(port.getPortId(), port);
            port.setNode(this);
        }
        for (Port port : ports) {
            LogCore.core.info("add port {} of class {}", port.getPortId(), port.getClass().getName());
            portsWaitForRun.add(port);
            port.post(port::startup);
        }
    }

    public void addMainThreadPort() {
        addPort(new MainThreadService());
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
    void awake(Port port) {
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
    void handleCall(Call call) {
        // 发送到此node的消息
        Port port = getPort(call.to.portID);
        if (port == null) {
            return;
        }
        port.getTerminal().addCall(SerializableManager.clone(call));
        awake(port);
    }

    public String getId() {
        return nodeId;
    }

    public void mainThreadUpdate() {
        if (mainThreadService != null) {
            mainThreadService.mainThreadUpdate();
        } else {
            mainThreadService = (MainThreadService) getPort(MainThreadService.class.getName());
            if (mainThreadService != null) {
                mainThreadService.mainThreadUpdate();
            } else {
                LogCore.core.error("not find mainThreadService");
            }
        }
    }
}
