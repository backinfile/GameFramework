package com.backinfile.GameFramework.core;

import com.backinfile.support.Time;

public abstract class Service extends Port {
    private long nextSecMills = 0;

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

    @Override
    void handleRequest(Call call) {
        ServiceProxyBase.handleRequest(this, call);
    }
}
