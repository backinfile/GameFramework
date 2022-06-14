package com.backinfile.GameFramework.proxy;

import com.backinfile.GameFramework.LogCore;
import com.backinfile.GameFramework.core.Port;
import com.backinfile.support.Time;
import com.backinfile.support.func.Action0;

/**
 * 此类中所有public的返回Task的函数会自动注册为代理函数
 * 不适用函数重载
 * 需要有空构造函数或一个id参数的构造函数
 */
public abstract class AsyncObject {
    private final long objId;
    private Port port;

    // 仅用于代理类中
    protected String targetPort;
    private long lastPulsePerSecTimer = 0;


    public AsyncObject() {
        this(0);
    }

    public AsyncObject(long objId) {
        this.objId = objId;
    }

    public long getObjId() {
        return objId;
    }

    public void onAttach(Port port) {
        this.port = port;
    }

    public void onDetach(Port port) {
        this.port = null;
    }

    public void casePulse() {
        pulse();

        long curTime = getCurTime();
        if (curTime / Time.SEC > lastPulsePerSecTimer / Time.SEC) {
            lastPulsePerSecTimer = curTime;
            pulsePerSec();
        }
    }

    public void pulse() {
    }

    public void pulsePerSec() {
    }


    public Port getPort() {
        return port;
    }

    public long getCurTime() {
        if (port != null) {
            return port.getTime();
        }
        return Time.getCurMillis();
    }

    public void post(Action0 action0) {
        if (port != null) {
            port.post(action0);
        } else {
            LogCore.core.warn("post action on {} ignore due to no port", this.getClass().getSimpleName());
        }
    }
}
