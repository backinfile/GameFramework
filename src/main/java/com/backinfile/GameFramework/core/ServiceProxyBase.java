package com.backinfile.GameFramework.core;

import com.backinfile.GameFramework.LogCore;
import com.backinfile.support.SysException;

import java.lang.reflect.Method;
import java.util.Map;

public class ServiceProxyBase {
    // 推送请求
    // TODO 主线程也可发送
    public static Task<Object> request(Port port, String targetPort, long targetObjId, int methodKey, Object... args) {
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


    // 处理请求
    @SuppressWarnings("unchecked")

    public static void handleRequest(Service service, Call call) {

        Class<?> proxyClass = Class

        AsyncObject asyncObj = port.getAsyncObj(call.to.objId);
        if (asyncObj == null) {
            LogCore.core.error("not find AsyncObject of " + call);
            return;
        }
        Map<Integer, Method> methodMap = proxyClassMethodMap.get(asyncObj.getClass());
        if (methodMap == null || !methodMap.containsKey(call.method)) {
            LogCore.core.error("not find method of " + asyncObj.getClass().getName() + " in " + call);
            return;
        }
        Method method = methodMap.get(call.method);
        try {
            Task<Object> task = (Task<Object>) method.invoke(asyncObj, call.args);
            task.whenComplete((obj, ex) -> {
                if (ex != null) {
                    Call callReturn = call.newErrorReturn(ex.getMessage());
                    service.getTerminal().returns(callReturn);
                    LogCore.core.warn("error in invoke origin method " + method, ex);
                } else {
                    Call callReturn = call.newCallReturn(obj);
                    service.getTerminal().returns(callReturn);
                }
            });
        } catch (Exception e) {
            LogCore.core.error("error in invoke origin method " + method, e);
        }
    }
}
