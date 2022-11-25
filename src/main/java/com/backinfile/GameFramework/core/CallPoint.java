package com.backinfile.GameFramework.core;

import com.backinfile.GameFramework.serialize.Serializable;

@Serializable
public class CallPoint {
    public String portID;
    public int modId;

    public CallPoint() {
    }

    public CallPoint(String portID, int modId) {
        this.portID = portID;
        this.modId = modId;
    }

    public CallPoint copy() {
        return new CallPoint(portID, modId);
    }


    @Override
    public String toString() {
        return "CallPoint{" +
                "portID='" + portID + '\'' +
                ", modId=" + modId +
                '}';
    }
}
