package com.backinfile.GameFramework.proxy;

import com.backinfile.GameFramework.Log;
import com.backinfile.support.SysException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class Proxy {
    /**
     * 获取代理类
     */
    public static <T extends AsyncObject> T getProxy(Class<T> clazz) {
        return getProxy(clazz, 0);
    }

    /**
     * 获取代理类
     */
    @SuppressWarnings("unchecked")
    public static <T extends AsyncObject> T getProxy(Class<T> clazz, int targetObjId) {
        if (!AsyncObject.class.isAssignableFrom(clazz)) {
            throw new SysException("only getProxy of AsyncObject's sub objects");
        }
        if (Modifier.isAbstract(clazz.getModifiers())) {
            throw new SysException("cannot getProxy of abstract class");
        }

        Constructor<?> constructor = ProxyManager.proxyClassMap.get(clazz);
        if (constructor == null) {
            throw new SysException("not find proxy of " + clazz.getName());
        }
        if (constructor.getParameterCount() == 0 && targetObjId != 0) {
            throw new SysException("targetObjId != 0 of" + clazz.getName());
        }

        try {
            Object obj = null;
            if (constructor.getParameterCount() == 0) {
                obj = constructor.newInstance();
            } else {
                obj = constructor.newInstance(targetObjId);
            }
            ((AsyncObject) obj).targetPort = ProxyManager.targetPortMap.get(clazz);
            return (T) obj;
        } catch (Exception e) {
            Log.core.error("error in create proxy instance of " + clazz.getName(), e);
        }
        return null;
    }
}
