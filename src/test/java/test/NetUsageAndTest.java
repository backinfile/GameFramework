package test;


import com.backinfile.GameFramework.net.*;
import com.backinfile.support.Utils;

import java.io.IOException;

public class NetUsageAndTest {

    public static class EchoServerHandler implements INetHandler {
        @Override
        public void onActive(ChannelConnection connection) {

        }

        @Override
        public void onInactive(ChannelConnection connection) {

        }

        @Override
        public void onRead(ChannelConnection connection) {
            GameMessage gameMessage = connection.pollGameMessage();
            System.out.println("read " + gameMessage.getMessage());
            connection.sendGameMessage(gameMessage);
        }

        @Override
        public void onException(ChannelConnection connection, Throwable cause) {

        }
    }

    public static class HelloClientHandler implements INetHandler {

        @Override
        public void onActive(ChannelConnection connection) {

            connection.sendGameMessage(GameMessage.build("hello"));
        }

        @Override
        public void onInactive(ChannelConnection connection) {

        }

        @Override
        public void onRead(ChannelConnection connection) {

            GameMessage gameMessage = connection.pollGameMessage();
            System.out.println("read " + gameMessage.getMessage());

            connection.close();
        }

        @Override
        public void onException(ChannelConnection connection, Throwable cause) {

        }
    }
}

class TestClient {
    public static void main(String[] args) throws IOException {
        GameMessage.setEnableStringType(true);
        Client client = new Client(new NetUsageAndTest.HelloClientHandler(), "", 10088);
        client.start();
        Utils.readExit();
        client.stopClient();
    }
}

class TestServer {
    public static void main(String[] args) {
        GameMessage.setEnableStringType(true);
        Server server = new Server(new NetUsageAndTest.EchoServerHandler(), 10088);
        server.start();
        Utils.readExit();
        server.stopServer();
    }
}


