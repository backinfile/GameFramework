



目标：

 1. ```java
    Node node = new Node();
    node.add(new MyPort());
    node.start();
    ```

	2. ```java
    AsyncObject connection = new Connection();
    Node.getCurrent().getPort(MyPort.class).add(connection);
    
    var proxy = Proxy.get(Connection.class);
    var proxy = Proxy.get(Connection.class, callPoint);
    var proxy = Proxy.get(Connection.class, nodeid, portid, objId);
    var proxy = Proxy.get(Connection.class, portid, objId);
    ```

    