package com.backinfile.GameFramework.core;

import com.backinfile.GameFramework.LogCore;
import com.backinfile.support.Time;
import com.backinfile.support.func.CommonFunction;

public abstract class Service extends Port {
    private long nextSecMills = 0;
    private MethodHub methodHub = null;

    public Service() {
        this.setPortId(this.getClass().getName());
    }

    @Override
    public void pulsePort() {
        super.pulsePort();
        pulse();

        long time = getTime();
        if (nextSecMills == 0 || time >= nextSecMills) {
            nextSecMills = time - time % Time.SEC + Time.SEC;
            pulsePerSec();
        }
    }

    @Override
    public void startup() {
        init();
        super.startup();
    }

    public abstract void init();

    public abstract void pulse();

    public abstract void pulsePerSec();

    @SuppressWarnings("unchecked")
    @Override
    void handleRequest(Call call) {
        if (methodHub == null) {
            methodHub = ServiceProxyBase.createMethodHub(this);
            if (methodHub == null) {
                LogCore.core.error("create methodHub error {}", this.getClass().getName());
                return;
            }
        }

        CommonFunction method = methodHub.get(call.method);
        if (method == null) {
            LogCore.core.error("not find methodKey:{} of:{}", call.method, this.getClass().getName());
            return;
        }
        try {
            Task<Object> task = (Task<Object>) method.invoke(call.args);
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
