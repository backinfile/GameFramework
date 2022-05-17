package com.backinfile.GameFramework.proxy;

import com.backinfile.GameFramework.core.Port;

public abstract class AsyncObject {
    private final long objId;
    private Port port;

    // 仅用于代理类中
    protected String targetPort;


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

    public Port getPort() {
        return port;
    }
}
