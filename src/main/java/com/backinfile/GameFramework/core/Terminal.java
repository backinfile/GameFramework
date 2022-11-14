package com.backinfile.GameFramework.core;

import com.backinfile.GameFramework.LogCore;
import com.backinfile.support.Time;
import com.backinfile.support.func.Action1;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * rpc终端--port
 */
public class Terminal {
    private final Queue<Call> calls = new ConcurrentLinkedQueue<>(); // 等待执行的Call队列
    private final HashMap<Long, WaitResult> waitingResponseList = new HashMap<>(); // 等待远程返回

    private Call lastInCall; // 上一个接受到的call
    private Call lastOutCall; // 上一个发送出去的call
    private long idAllot = 1; // 分配id
    private final Node mNode;
    private final Port mPort;

    // 监听失效时间
    public static final long CALL_EXPIRE_TIME = 30 * Time.DAY;

    public Terminal(Node node, Port port) {
        this.mPort = port;
        this.mNode = node;
    }

    /**
     * 接受一个来自远程的调用
     */
    public void addCall(Call call) {
        calls.add(call);
    }

    /**
     * 获取上一次执行（或正在执行）的rpc调用
     */
    public Call getLastInCall() {
        return lastInCall;
    }

    /**
     * 获取上一次自身发起的请求的call对象
     * （通常用于多层级rpc嵌套返回）
     */
    public Call getLastOutCall() {
        return lastOutCall;
    }

    /**
     * 发起新的rpc调用
     * 由此terminal发送新call到其他terminal，必须在port线程中发送
     */
    public void sendNewCall(long id, CallPoint to, int method, Object[] args) {
        Call call = Call.newCall(id, getLocalCallPoint(), to.copy(), method, args);
        lastOutCall = call;
        mNode.handleCall(call);
    }

    private CallPoint getLocalCallPoint() {
        return new CallPoint(mPort.getPortId(), 0);
    }

    public long applyId() {
        return idAllot++;
    }

    /**
     * rpc返回
     */
    public void returns(Call call) {
        mNode.handleCall(call);
    }

    public void pulse() {
        executeInCall();


        // 清理超时的listen
        List<Long> toRemove = new ArrayList<>();
        for (Map.Entry<Long, WaitResult> entry : waitingResponseList.entrySet()) {
            if (entry.getValue().isExpire()) {
                toRemove.add(entry.getKey());
            }
        }
        for (long id : toRemove) {
            LogCore.core.warn("time out, clear rpc wait callId:{} port:{}", id, mPort.getPortId());
            WaitResult waitResult = waitingResponseList.remove(id);
            for (WaitResult.Callback callback : waitResult.callbackHandlers) {
                try {
                    Result result = new Result();
                    result.setErrorString("rpc time out");
                    callback.action.invoke(result);
                } catch (Exception e) {
                    LogCore.core.error("run rpc result callbackHandler function error", e);
                }
            }
        }
    }

    /**
     * 监听rpc调用执行结果
     */
    public void listenOutCall(long callId, Action1<IResult> action) {
        WaitResult waitResult = waitingResponseList.get(callId);
        if (waitResult == null) {
            waitResult = new WaitResult();
            waitResult.expireTime = Time.getCurMillis() + CALL_EXPIRE_TIME;
            waitingResponseList.put(callId, waitResult);
        }
        waitResult.addCallback(action);
    }

    public void setTimeout(long callId, long timeout) {
        WaitResult waitResult = waitingResponseList.get(callId);
        if (waitResult == null) {
            waitResult = new WaitResult();
            waitingResponseList.put(callId, waitResult);
        }
        waitResult.expireTime = Time.getCurMillis() + timeout;
    }

    /**
     * 处理收到的rpc请求
     */
    public void executeInCall() {
        while (true) {
            Call call = calls.poll();
            if (call == null)
                break;
            try {
                if (call.type == Call.RPC_TYPE_CALL) {
                    invoke(call);
                } else if (call.type == Call.RPC_TYPE_CALL_RETURN) {
                    processCallReturn(call);
                } else {
                    LogCore.core.error("unknown rpc call type {}", call.type);
                }
            } catch (Exception e) {
                LogCore.core.error("error in execute inCall", e);
            }
        }
    }

    /**
     * 前往对应port执行来自远程的调用
     */
    private void invoke(Call call) {
        lastInCall = call;

        try {
            mPort.handleRequest(call);
        } catch (Exception e) {
            LogCore.core.error("handleRequest error of " + mPort.getClass().getName(), e);
        }
    }

    /**
     * rpc调用返回处理，触发监听事件
     */
    private void processCallReturn(Call call) {
        if (!waitingResponseList.containsKey(call.id)) {
            return;
        }
        WaitResult waitResult = waitingResponseList.remove(call.id);
        for (WaitResult.Callback callback : waitResult.callbackHandlers) {
            try {
                Result result = new Result(call.args);
                result.setErrorString(call.errorString);
                callback.action.invoke(result);
            } catch (Exception e) {
                LogCore.core.error("run rpc result callbackHandler function error", e);
            }
        }
    }
}
