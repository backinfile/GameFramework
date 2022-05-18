package com.backinfile.GameFramework.net;

import com.backinfile.GameFramework.LogCore;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

public class Server extends Thread {
    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    private final int port;
    private static final AtomicLong idMax = new AtomicLong(1);
    private final ServerHandler serverHandler;

    public Server(int port) {
        this(null, port);
    }

    public Server(ServerHandler serverHandler, int port) {
        this.serverHandler = serverHandler;
        this.port = port;
        idMax.getAndSet(1);
    }

    @Override
    public void run() {
        try {
            startServer(port);
        } catch (InterruptedException e) {
            LogCore.server.error("listen start error", e);
        }
    }

    private void startServer(int port) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new Decoder(), new Encoder(), serverHandler != null ? serverHandler : new ServerHandler());
                        }
                    });

            // 第四步，开启监听
            Channel channel = b.bind(port).sync().channel();
            LogCore.server.info("start listen:{}", port);

            countDownLatch.await();
            channel.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void stopServer() {
        countDownLatch.countDown();
    }


    public static class ServerHandler extends ChannelInboundHandlerAdapter {
        protected ChannelConnection connection;

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            Channel channel = ctx.channel();
            connection = new ChannelConnection(idMax.getAndIncrement(), channel);
            LogCore.server.info("channelActive address:{} id:{}", channel.remoteAddress(), connection.getId());
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            connection.addInput((byte[]) msg);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            Channel channel = ctx.channel();
            LogCore.server.info("channelInactive address:{} id:{}", channel.remoteAddress(), connection.getId());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            Channel channel = ctx.channel();
            LogCore.server.error("exceptionCaught address:{} id:{} error:{} {}", channel.remoteAddress(),
                    connection.getId(), cause.getClass().getName(), cause.getMessage());
        }
    }
}