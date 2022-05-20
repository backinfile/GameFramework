package com.backinfile.GameFramework.core;

import com.backinfile.GameFramework.serialize.Serializable;

import java.util.Arrays;

@Serializable
public class Call {
    public static final int RPC_TYPE_CALL = 0;
    public static final int RPC_TYPE_CALL_RETURN = 1; // 与回复的call同id

    public long id;
    public CallPoint to;
    public CallPoint from;

    public int type;
    public int method;
    public Object[] args = null;
    public String errorString = "";


    public Call() {
    }

    public static Call newCall(long id, CallPoint from, CallPoint to, int method, Object[] args) {
        Call call = new Call();
        call.id = id;
        call.from = from;
        call.to = to;
        call.args = args;
        call.method = method;
        call.type = RPC_TYPE_CALL;
        return call;
    }

    public Call newCallReturn(Object[] args) {
        Call callReturn = new Call();
        callReturn.id = id;
        callReturn.from = to.copy();
        callReturn.to = from.copy();
        callReturn.args = args;
        callReturn.type = RPC_TYPE_CALL_RETURN;
        return callReturn;
    }

    public Call newErrorReturn(String error) {
        Call callReturn = new Call();
        callReturn.from = to.copy();
        callReturn.to = from.copy();
        callReturn.id = id;
        callReturn.errorString = error;
        callReturn.type = RPC_TYPE_CALL_RETURN;
        return callReturn;
    }

    @Override
    public String toString() {
        return "Call{" +
                "id=" + id +
                ", to=" + to +
                ", from=" + from +
                ", type=" + type +
                ", method=" + method +
                ", args=" + Arrays.toString(args) +
                ", error='" + errorString + '\'' +
                '}';
    }
}
