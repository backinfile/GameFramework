package com.backinfile.GameFramework.db;

import java.util.List;

public interface ILoadProvider {

    <T extends EntityBase> T querySingle(Class<T> clazz, long id);

    <T extends EntityBase> List<T> queryAll(Class<T> clazz);

    <T extends EntityBase> List<T> queryAllByPlayerId(Class<T> clazz, long playerId);

    void open();

    void close();
}
