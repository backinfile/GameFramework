package com.backinfile.GameFramework.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class NetUtils {
    public static DatagramPacket buildBroadcastMsg(String msg, int port) {
        return new DatagramPacket(Unpooled.copiedBuffer(msg, StandardCharsets.UTF_8),
                new InetSocketAddress("255.255.255.255", port));
    }

    public static DatagramPacket buildMsg(String msg, InetSocketAddress address) {
        return new DatagramPacket(Unpooled.copiedBuffer(msg, StandardCharsets.UTF_8), address);
    }

    public static String getString(DatagramPacket msg) {
        ByteBuf byteBuf = msg.content();
        return byteBuf.readCharSequence(byteBuf.readableBytes(), StandardCharsets.UTF_8).toString();
    }
}
