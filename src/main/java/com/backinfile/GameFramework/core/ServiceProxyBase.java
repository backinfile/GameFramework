package com.backinfile.GameFramework.core;

import com.backinfile.support.SysException;
import com.backinfile.support.func.CommonFunction;

import java.util.HashMap;
import java.util.Map;

public abstract class ServiceProxyBase {
    private static final Map<String, ServiceProxyBase> proxyBaseMap = new HashMap<>();

    protected static void setProxyBase(String name, ServiceProxyBase proxyBase) {
        proxyBaseMap.put(name, proxyBase);
    }

    static ServiceProxyBase getProxyBase(Service service) {
        return proxyBaseMap.get(service.getClass().getName());
    }


    private final Map<Integer, CommonFunction> methodMap = new HashMap<>();

    protected void setMethod(int methodKey, CommonFunction commonFunction) {
        methodMap.put(methodKey, commonFunction);
    }

    CommonFunction getMethod(int methodKey) {
        return methodMap.get(methodKey);
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
