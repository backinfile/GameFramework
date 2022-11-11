package com.backinfile.GameFramework;

import com.backinfile.GameFramework.core.Node;
import com.backinfile.GameFramework.core.Service;
import com.backinfile.GameFramework.db.DBDirectProvider;
import com.backinfile.GameFramework.db.DBManager;
import com.backinfile.GameFramework.event.EventEx;
import com.backinfile.GameFramework.serialize.SerializableManager;
import com.ea.async.Async;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class GameStartUp {
    public static final String PACKAGE_PATH = "com.backinfile.GameFramework";

    public static void initAll(List<String> packagePaths, List<ClassLoader> classLoaders) {
        List<String> finalPackagePaths = new ArrayList<>(packagePaths);
        finalPackagePaths.add(PACKAGE_PATH);
        List<ClassLoader> finalClassLoaders = new ArrayList<>(classLoaders);
        finalClassLoaders.add(GameStartUp.class.getClassLoader());

        Async.init();
        SerializableManager.registerAll(finalPackagePaths, finalClassLoaders); // 序列化支持
        DBManager.registerAll(finalPackagePaths, finalClassLoaders); // db支持
        EventEx.registerAll(finalPackagePaths, finalClassLoaders); // 事件支持
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
