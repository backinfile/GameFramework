package com.backinfile.GameFramework.core;

import com.backinfile.support.timer.TimerQueue;

public abstract class ServiceMod<S extends Service> implements IService {
    S service;

    public ServiceMod() {
    }

    public final S getService() {
        return service;
    }

    public final TimerQueue getTimerQueue() {
        return service.getTimerQueue();
    }

}
