package com.backinfile.GameFramework.core;

import com.backinfile.GameFramework.service.MainThreadService;
import com.backinfile.support.SysException;
import com.backinfile.support.func.CommonFunction;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ServiceProxyBase {
    private static final Map<String, Map<Integer, CommonFunction>> serviceMethodMap = new ConcurrentHashMap<>();

    protected static void addMethodMap(String name, Map<Integer, CommonFunction> methodMap) {
        serviceMethodMap.put(name, Collections.unmodifiableMap(methodMap));
    }

    public static Map<Integer, CommonFunction> getMethodMap(Service service) {
        return serviceMethodMap.get(service.getClass().getName());
    }


    @SuppressWarnings("all")
    // 推送请求
    protected static Task request(Port port, String targetPort, long targetObjId, int methodKey, Object... args) {
        if (port == null) {
            return Task.failure(new SysException("rpc not request from a port!"));
        }
        Terminal terminal = port.getTerminal();
        long callId = terminal.applyId();

        if (port instanceof MainThreadService) { // 从主线程发送
            Task task = new Task();
            terminal.listenOutCall(callId, ir -> {
                ((MainThreadService) port).waitMainThreadUpdate().whenComplete((r, ex) -> completeReturnTask(ir, task));
            });
            terminal.sendNewCall(callId, new CallPoint(targetPort, targetObjId), methodKey, args);
            return task;

        } else { // 从其他Port发送 需要先进行监听，然后发送消息，避免返回过快导致没有收到返回
            Task task = new Task();
            terminal.listenOutCall(callId, ir -> completeReturnTask(ir, task));
            terminal.sendNewCall(callId, new CallPoint(targetPort, targetObjId), methodKey, args);
            return task;
        }
    }

    private static void completeReturnTask(IResult ir, Task<Object> task) {
        if (ir.hasError()) {
            task.completeExceptionally(new SysException("rpc return error:" + ir.getErrorString()));
        } else {
            task.complete(ir.getResult());
        }
    }

}
