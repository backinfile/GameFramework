package com.backinfile.GameFramework.core;

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
    // TODO 主线程也可发送
    protected static Task request(Port port, String targetPort, long targetObjId, int methodKey, Object... args) {
        if (port == null) {
            return Task.failure(new SysException("rpc not request from a port!"));
        }
        port.getTerminal().sendNewCall(new CallPoint(targetPort, targetObjId), methodKey, args);
        Call lastOutCall = port.getTerminal().getLastOutCall();
        Task task = new Task();
        port.getTerminal().listenOutCall(lastOutCall.id, ir -> {
            if (ir.hasError()) {
                task.completeExceptionally(new SysException("rpc return error:" + ir.getErrorString()));
            } else {
                task.complete(ir.getResult());
            }
        });
        return task;
    }

}
