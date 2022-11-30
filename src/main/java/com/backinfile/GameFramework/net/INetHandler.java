package com.backinfile.GameFramework.net;

public interface INetHandler {
    void onActive(ChannelConnection connection);

    void onInactive(ChannelConnection connection);

    void onRead(ChannelConnection connection);

    void onException(ChannelConnection connection, Throwable cause);
    
}
