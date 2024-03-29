package com.backinfile.GameFramework.net;

import com.backinfile.GameFramework.LogCore;
import com.backinfile.support.Utils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

public class Client extends Thread {
    public static Channel Channel = null;
    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    private final String host;
    private final int port;
    private final INetHandler netHandler;

    public Client(INetHandler netHandler, String host, int port) {
        this.netHandler = netHandler;
        this.host = Utils.isNullOrEmpty(host) ? "127.0.0.1" : host;
        this.port = port;
    }


    public Client(String host, int port) {
        this(null, host, port);
    }

    public Client(int port) {
        this(null, "127.0.0.1", port);
    }

    @Override
    public void run() {
        try {
            startClient(host, port);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startClient(String host, int port) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class).remoteAddress(new InetSocketAddress(host, port))
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) {
                    ChannelPipeline pipeline = socketChannel.pipeline();
                    pipeline.addLast(new Decoder(), new Encoder(), new ClientHandler(Client.this, netHandler));
                }
            });

            LogCore.client.info("start connect {}:{}", host, port);
            Channel = b.connect().sync().channel();
            LogCore.client.info("connected: {}:{}", host, port);

            countDownLatch.await();
            Channel.close().sync();
            LogCore.client.info("connection close {}:{}", host, port);
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    public void stopClient() {
        countDownLatch.countDown();
    }

    private static final class ClientHandler extends ChannelInboundHandlerAdapter {
        private final Client client;
        private ChannelConnection connection = null;
        private final INetHandler netHandler;

        public ClientHandler(Client client, INetHandler netHandler) {
            this.client = client;
            this.netHandler = netHandler;
        }


        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            Channel channel = ctx.channel();
            connection = new ChannelConnection(0, channel);
            LogCore.client.info("channelActive address:{} id:{}", channel.remoteAddress(), connection.getId());

            if (netHandler != null) {
                netHandler.onActive(connection);
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            Channel channel = ctx.channel();
            LogCore.client.info("channelInactive address:{} id:{}", channel.remoteAddress(), connection.getId());
            client.stopClient();

            if (netHandler != null) {
                netHandler.onInactive(connection);
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            connection.addInput((byte[]) msg);

            if (netHandler != null) {
                netHandler.onRead(connection);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            Channel channel = ctx.channel();
            LogCore.client.error("exceptionCaught address:{} id:{} error:{} {}", channel.remoteAddress(),
                    connection.getId(), cause.getClass().getName(), cause.getMessage());


            if (netHandler != null) {
                netHandler.onException(connection, cause);
            }
        }
    }
}
