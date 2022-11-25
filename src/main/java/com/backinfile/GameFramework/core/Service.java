package com.backinfile.GameFramework.core;

import com.backinfile.GameFramework.LogCore;
import com.backinfile.support.Time;
import com.backinfile.support.func.CommonFunction;

import java.util.HashMap;
import java.util.Map;

public abstract class Service extends Port implements IService {
    private long nextSecMills = 0;
    private final Map<Integer, ServiceMod<?>> serviceModMap = new HashMap<>();
    private Map<Integer, CommonFunction> methodMap = null;
    private final Map<Integer, Map<Integer, CommonFunction>> serviceModMethodMap = new HashMap<>();

    public Service() {
        this.setPortId(this.getClass().getName());
    }

    @Override
    public void pulsePort() {
        super.pulsePort();
        pulse();
        for (ServiceMod<?> mod : serviceModMap.values()) {
            mod.pulse();
        }

        long time = getTime();
        if (nextSecMills == 0 || time >= nextSecMills) {
            nextSecMills = time - time % Time.SEC + Time.SEC;
            pulsePerSec();
            for (ServiceMod<?> mod : serviceModMap.values()) {
                mod.pulsePerSec();
            }
        }
    }

    @Override
    public void startup() {
        init();
        for (ServiceMod<?> mod : serviceModMap.values()) {
            mod.init();
            LogCore.core.info("service mod init over {}", mod.getClass().getName());
        }
        super.startup();
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public final <S extends Service> Service addServiceMod(ServiceMod<S>... mods) {
        for (ServiceMod<S> mod : mods) {
            int id = mod.getClass().getName().hashCode();
            mod.service = (S) this;
            serviceModMap.put(id, mod);
        }
        if (isStartupOver()) {
            for (ServiceMod<?> mod : mods) {
                mod.init();
            }
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public <M extends ServiceMod<?>> M getMod(Class<M> serviceModClass) {
        return (M) serviceModMap.get(serviceModClass.getName().hashCode());
    }

    @Override
    void handleRequest(Call call) {
        int modId = call.to.modId;
        if (modId == 0) {
            if (methodMap == null) {
                methodMap = ServiceProxyBase.getMethodMap(this, 0);
                if (methodMap == null) {
                    LogCore.core.error("getProxyBase error {}", this.getClass().getName());
                    return;
                }
            }
            invokeCall(call, methodMap, this);


        } else {
            ServiceMod<?> serviceMod = serviceModMap.get(modId);
            if (serviceMod == null) {
                LogCore.core.error("not found service mod on service " + this.getClass().getName());
                return;
            }

            Map<Integer, CommonFunction> methodMap = serviceModMethodMap.get(modId);
            if (methodMap == null) {
                methodMap = ServiceProxyBase.getMethodMap(this, modId);
                serviceModMethodMap.put(modId, methodMap);
            }
            if (methodMap == null) {
                LogCore.core.error("getProxyBase error {}", this.getClass().getName());
                return;
            }

            invokeCall(call, methodMap, serviceMod);
        }
    }


    @SuppressWarnings("unchecked")
    private void invokeCall(Call call, Map<Integer, CommonFunction> methodMap, Object service) {
        int modId = call.to.modId;

        CommonFunction method = methodMap.get(call.method);
        if (method == null) {
            LogCore.core.error("not find methodKey:{} of:{}", call.method, this.getClass().getName());
            return;
        }
        try {
            Task<Object> task = (Task<Object>) method.invoke(service, call.args);
            task.whenComplete((obj, ex) -> {
                if (ex != null) {
                    Call callReturn = call.newErrorReturn(ex.getMessage());
                    getTerminal().returns(callReturn);
                    LogCore.core.warn("error in invoke origin method " + method, ex);
                } else {
                    Call callReturn = call.newCallReturn(obj);
                    getTerminal().returns(callReturn);
                }
            });
        } catch (Exception e) {
            LogCore.core.error("error in invoke origin method " + method, e);
        }
    }
}
