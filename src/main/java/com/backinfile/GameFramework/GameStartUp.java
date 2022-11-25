package com.backinfile.GameFramework;

import com.backinfile.GameFramework.core.Node;
import com.backinfile.GameFramework.core.Service;
import com.backinfile.GameFramework.core.ServiceMod;
import com.backinfile.GameFramework.db.DBDirectProvider;
import com.backinfile.GameFramework.db.DBManager;
import com.backinfile.GameFramework.event.EventEx;
import com.backinfile.GameFramework.gen.GenTools;
import com.backinfile.GameFramework.gen.ServiceGenInfo;
import com.backinfile.GameFramework.serialize.SerializableManager;
import com.backinfile.support.SysException;
import com.ea.async.Async;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GameStartUp {
    public static final String PACKAGE_PATH = "com.backinfile.GameFramework";
    public static final Thread MainThread = Thread.currentThread();

    // 提供一个当前工作项目的类 进行初始化
    public static void initAll(Class<?> projClass) {
        initAll(Collections.singletonList(projClass.getPackage().getName()), Collections.singletonList(projClass.getClassLoader()));
    }

    // 进行初始化
    public static void initAll(List<String> packagePaths, List<ClassLoader> classLoaders) {
        Async.init(); // 异步支持
        SerializableManager.registerAll(packagePaths, classLoaders); // 序列化支持
        DBManager.registerAll(packagePaths, classLoaders); // db支持
        EventEx.registerAll(packagePaths, classLoaders); // 事件支持
    }

    public static void enableDirectDB(String path) {
        DBManager.enableSqlLog(true);
        DBDirectProvider dbDirectProvider = DBDirectProvider.createInstance(path);
        DBManager.setSaveProvider(dbDirectProvider);
        dbDirectProvider.open();
    }

    public static void startUp(Service... services) {
        Node node = new Node();
        node.addPort(services);
        node.startUp();
        node.waitAllPortStartupFinish();
        node.join();
    }

    public static void startUpUsingMainThread(Service... services) {
        Node node = new Node();
        node.addMainThreadPort();
        node.addPort(services);
        node.startUp();
        node.waitAllPortStartupFinish();
    }


    // 提供一个包内的类，查找并创建所有service和serviceMod
    public static Service[] createAllService(Class<?> projClass) {
        Map<Class<?>, ServiceGenInfo> allService = GenTools.findAllService(projClass);
        List<Service> resultList = new ArrayList<>();

        for (ServiceGenInfo info : allService.values()) {
            try {
                Service service = (Service) info.serviceClass.getConstructor().newInstance();
                for (Class<?> modClass : info.serviceModClass) {
                    service.addServiceMod((ServiceMod<?>) modClass.getConstructor().newInstance());
                }
                resultList.add(service);
                LogCore.core.info("create service {} and {} mod(s)", info.serviceClass.getName(), info.serviceModClass.size());
            } catch (Exception e) {
                throw new SysException("create service error " + info.serviceClass.getName(), e);
            }
        }
        return resultList.toArray(new Service[0]);
    }
}
