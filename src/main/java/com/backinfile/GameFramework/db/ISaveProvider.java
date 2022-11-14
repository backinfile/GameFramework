package com.backinfile.GameFramework.db;

public interface ISaveProvider {
    void insert(EntityBase obj);

    void update(EntityBase obj);

    void delete(EntityBase obj);

    void open();

    void close();
}
