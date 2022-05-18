package com.backinfile.GameFramework.core;

import com.backinfile.GameFramework.LogCore;
import com.backinfile.GameFramework.proxy.ProxyManager;
import com.backinfile.support.Time;
import com.backinfile.support.func.Action1;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * rpc终端--port
 */
public class Terminal implements ITerminal {
    private final Queue<Call> calls = new ConcurrentLinkedQueue<>(); // 等待执行的Call队列
    private final HashMap<Long, WaitResult> waitingResponseList = new HashMap<>(); // 等待远程返回

    private Call lastInCall; // 上一个接受到的call
    private Call lastOutCall; // 上一个发送出去的call
    private long idAllot = 1; // 分配id
    private final Node mNode;
    private final Port mPort;

    // 监听失效时间
    public static final long CALL_EXPIRE_TIME = 30 * Time.SEC;

    public Terminal(Node node, Port port) {
        this.mPort = port;
        this.mNode = node;
    }

    // 此terminal接受到新call
    @Override
    public void addCall(Call call) {
        calls.add(call);
    }

    @Override
    public Call getLastInCall() {
        return lastInCall;
    }

    public Call getLastOutCall() {
        return lastOutCall;
    }

    /**
     * 由此terminal发送新call到其他terminal，必须在port线程中发送
     */
    @Override
    public void sendNewCall(CallPoint to, int method, Object[] args) {
        Call call = Call.newCall(applyId(), getLocalCallPoint(), to.copy(), method, args);
        lastOutCall = call;
        mNode.handleCall(call);
    }

    private CallPoint getLocalCallPoint() {
        return new CallPoint(mPort.getPortId(), 0);
    }

    private long applyId() {
        return idAllot++;
    }

    @Override
    public void returns(Call call, Object... results) {
        Call callReturn = call.newCallReturn(results);
        mNode.handleCall(callReturn);
    }

    @Override
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

    @Override
    public void listenOutCall(long callId, Action1<IResult> action) {
        WaitResult waitResult = waitingResponseList.get(callId);
        if (waitResult == null) {
            waitResult = new WaitResult();
            waitResult.expireTime = Time.getCurMillis() + CALL_EXPIRE_TIME;
            waitingResponseList.put(callId, waitResult);
        }
        waitResult.addCallback(action);
    }

    @Override
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
        ProxyManager.handleRequest(mPort, call);
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
