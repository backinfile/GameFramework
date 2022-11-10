package com.backinfile.GameFramework.net;


import com.backinfile.support.Utils;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;

public class NetUsageAndTest {

    public static class EchoServerHandler extends Server.ServerHandler {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            super.channelRead(ctx, msg);

            GameMessage gameMessage = getConnection().pollGameMessage();
            System.out.println("read " + gameMessage.getMessage());
            getConnection().sendGameMessage(gameMessage);
        }
    }

    public static class HelloClientHandler extends Client.ClientHandler {
        public HelloClientHandler(Client client) {
            super(client);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            super.channelActive(ctx);

            getConnection().sendGameMessage(GameMessage.build("hello"));
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            super.channelRead(ctx, msg);

            GameMessage gameMessage = getConnection().pollGameMessage();
            System.out.println("read " + gameMessage.getMessage());

//            getConnection().close();
        }
    }
}

class TestClient {
    public static void main(String[] args) throws IOException {
        GameMessage.setEnableStringType(true);
        Client client = new Client(NetUsageAndTest.HelloClientHandler::new, "", 10088);
        client.start();
        Utils.readExit();
        client.stopClient();
    }
}

class TestServer {
    public static void main(String[] args) {
        GameMessage.setEnableStringType(true);
        Server server = new Server(NetUsageAndTest.EchoServerHandler::new, 10088);
        server.start();
        Utils.readExit();
        server.stopServer();
    }
}


