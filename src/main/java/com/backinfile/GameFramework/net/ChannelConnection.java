package com.backinfile.GameFramework.net;

import com.backinfile.support.Time;
import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class ChannelConnection implements Delayed, Connection {
    private final Channel channel;
    private final ConcurrentLinkedQueue<byte[]> inputList = new ConcurrentLinkedQueue<>();
    private long time;
    public static final int HZ = 1;

    private final long id;

    public ChannelConnection(long id, Channel channel) {
        this.channel = channel;
        this.id = id;
    }

    @Override
    public long getId() {
        return id;
    }

    public void pulse() {
        time = Time.getCurMillis();
    }

    @Override
    public GameMessage pollGameMessage() {
        byte[] data = inputList.poll();
        if (data == null) {
            return null;
        }
        GameMessage gameMessage;
        try {
            gameMessage = GameMessage.build(data, 0, data.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return gameMessage;
    }

    @Override
    public void sendGameMessage(GameMessage gameMessage) {
        channel.writeAndFlush(gameMessage.getBytes());
    }

    /**
     * 添加输入
     */
    public void addInput(byte[] data) {
        inputList.add(data);
    }

    public boolean isAlive() {
        return channel.isActive();
    }

    @Override
    public int compareTo(Delayed o) {
        ChannelConnection connection = (ChannelConnection) o;
        return Long.compare(time, connection.time);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return time + 1000 / HZ - Time.getCurMillis();
    }

    public void close() {
        channel.close();
    }

}
