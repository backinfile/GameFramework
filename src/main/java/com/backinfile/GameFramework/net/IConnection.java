package com.backinfile.GameFramework.net;

public interface IConnection {
    long getId();

    void pulse();

    boolean isAlive();

    GameMessage pollGameMessage();

    void sendGameMessage(GameMessage gameMessage);

    void close();

}
