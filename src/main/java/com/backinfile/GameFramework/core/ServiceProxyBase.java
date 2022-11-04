package com.backinfile.GameFramework.core;

import com.backinfile.support.SysException;

public abstract class ServiceProxyBase {

    static MethodHub createMethodHub(Service service) {
        throw new SysException("unimplemented");
    }

    // 推送请求
    // TODO 主线程也可发送
    protected static Task<Object> request(Port port, String targetPort, long targetObjId, int methodKey, Object... args) {
        if (port == null) {
            return Task.failure(new SysException("rpc not request from a port!"));
        }
        port.getTerminal().sendNewCall(new CallPoint(targetPort, targetObjId), methodKey, args);
        Call lastOutCall = port.getTerminal().getLastOutCall();
        Task<Object> task = new Task<>();
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
