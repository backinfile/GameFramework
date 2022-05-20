package com.backinfile.GameFramework.core;

import com.backinfile.GameFramework.serialize.Serializable;

@Serializable
public class CallPoint {
    public String portID;
    public long objId;

    public CallPoint() {
    }

    public CallPoint(String portID, long objId) {
        this.portID = portID;
        this.objId = objId;
    }

    public CallPoint copy() {
        return new CallPoint(portID, objId);
    }


    @Override
    public String toString() {
        return "CallPoint{" +
                "portID='" + portID + '\'' +
                ", objId=" + objId +
                '}';
    }
}
