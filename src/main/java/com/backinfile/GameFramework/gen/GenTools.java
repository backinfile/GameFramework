package com.backinfile.GameFramework.gen;

import com.backinfile.GameFramework.LogCore;
import com.backinfile.GameFramework.core.RPCMethod;
import com.backinfile.GameFramework.core.Service;
import com.backinfile.GameFramework.core.Task;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenTools {
    private static final String SERVICE_PROXY_PREFIX = "Proxy";

    public static boolean genServiceProxy(Class<? extends Service> serviceClass, String packageName, String targetPath) {
        String className = serviceClass.getSimpleName();
        String proxyClassName = serviceClass.getSimpleName() + SERVICE_PROXY_PREFIX;


        Map<String, Object> rootMap = new HashMap<>();
        rootMap.put("className", className);
        rootMap.put("proxyClassName", proxyClassName);

        List<Object> methods = new ArrayList<>();
        rootMap.put("methods", methods);

        for (Method method : serviceClass.getMethods()) {
            if (Modifier.isStatic(method.getModifiers()) || Modifier.isAbstract(method.getModifiers())) {
                continue;
            }
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            if (!method.isAnnotationPresent(RPCMethod.class)) {
                continue;
            }
            if (!Task.class.isAssignableFrom(method.getReturnType())) {
                LogCore.gen.error("rpc方法的返回类型需要是Task method:{}", method);
                continue;
            }
            if (method.getParameterCount() > 8) {
                LogCore.gen.error("rpc方法的参数太多了 method:{}", method);
                continue;
            }

            Map<String, Object> methodInfo = new HashMap<>();
            methodInfo.put("name", method.getName());
            methods.add(methodInfo);
        }

        return true;
    }
}
