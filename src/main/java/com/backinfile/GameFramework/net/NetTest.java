package com.backinfile.GameFramework.net;


import com.backinfile.support.Utils;

import java.io.IOException;

public class NetTest {

}


class TestClient {
    public static void main(String[] args) throws IOException {
        Client client = new Client(10088);
        client.start();
        Utils.readExit();
    }
}

class TestServer {
    public static void main(String[] args) {
        Server server = new Server(10088);
        server.start();

        Utils.readExit();
    }
}