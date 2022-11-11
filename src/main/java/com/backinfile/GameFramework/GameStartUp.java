package com.backinfile.GameFramework;

import com.backinfile.GameFramework.core.Node;
import com.backinfile.GameFramework.core.Service;
import com.backinfile.GameFramework.db.DBDirectProvider;
import com.backinfile.GameFramework.db.DBManager;
import com.backinfile.GameFramework.event.EventEx;
import com.backinfile.GameFramework.serialize.SerializableManager;
import com.ea.async.Async;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class GameStartUp {
    public static final String PACKAGE_PATH = "com.backinfile.GameFramework";

    // 提供一个当前工作项目的类 进行初始化
    public static void initAll(Class<?> clazz) {
        initAll(Collections.singletonList(clazz.getPackage().getName()), Collections.singletonList(clazz.getClassLoader()));
    }

    // 进行初始化
    public static void initAll(List<String> packagePaths, List<ClassLoader> classLoaders) {

        Async.init();
        SerializableManager.registerAll(packagePaths, classLoaders); // 序列化支持
        DBManager.registerAll(packagePaths, classLoaders); // db支持
        EventEx.registerAll(packagePaths, classLoaders); // 事件支持
    }

    public static void enableDirectDB() {
        DBManager.enableSqlLog(true);
        DBDirectProvider dbDirectProvider = DBDirectProvider.getInstance();
        DBManager.setSaveProvider(dbDirectProvider);
        dbDirectProvider.open();
    }

    @SafeVarargs
    public static void startUp(Supplier<Service>... suppliers) {
        Node node = new Node();
        for (Supplier<Service> supplier : suppliers) {
            node.addPort(supplier.get());
        }
        node.startUp();
        node.waitAllPortStartupFinish();
        node.join();
    }
}
