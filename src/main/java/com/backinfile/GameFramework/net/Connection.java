package com.backinfile.GameFramework.net;

import java.util.concurrent.Delayed;

public interface Connection extends Delayed {
    public long getId();

    public void pulse();

    public boolean isAlive();

    public GameMessage pollGameMessage();

    public void sendGameMessage(GameMessage gameMessage);

    public void close();

}
