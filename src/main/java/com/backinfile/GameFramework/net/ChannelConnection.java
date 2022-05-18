package com.backinfile.GameFramework.net;

import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ChannelConnection implements IConnection {
    private final long id;
    private final Channel channel;
    private final ConcurrentLinkedQueue<byte[]> inputList = new ConcurrentLinkedQueue<>();


    public ChannelConnection(long id, Channel channel) {
        this.channel = channel;
        this.id = id;
    }

    @Override
    public long getId() {
        return id;
    }

    public void pulse() {
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

    public void close() {
        channel.close();
    }

}
