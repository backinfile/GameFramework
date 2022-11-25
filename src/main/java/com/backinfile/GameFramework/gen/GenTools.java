package com.backinfile.GameFramework.gen;

import com.backinfile.GameFramework.LogCore;
import com.backinfile.GameFramework.core.RPCMethod;
import com.backinfile.GameFramework.core.Service;
import com.backinfile.GameFramework.core.ServiceMod;
import com.backinfile.GameFramework.core.Task;
import com.backinfile.support.SysException;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 * 代码生成工具
 * 如果生成函数参数是默认的名字(arg0)，需要开启编译参数-parameters
 */
public class GenTools {
    private static final String SERVICE_PROXY_PREFIX = "Proxy";

    public static String getAbsolutePath(String pathInProj) {
        return new File(System.getProperty("user.dir"), pathInProj).getAbsolutePath();
    }

    public static void genAllServiceProxy(Class<?> projClass, String targetPackageName, String targetPath, boolean clearDir) {
        if (clearDir) {
            File file = new File(targetPath);
            if (file.exists() && file.isDirectory()) {
                File[] listFiles = file.listFiles();
                if (listFiles != null) {
                    LogCore.gen.info("clear dir {}", file.getAbsolutePath());
                    for (File subFile : listFiles) {
                        if (subFile.isFile()) {
                            if (!subFile.delete()) {
                                LogCore.gen.error("clear file error {}", subFile.getAbsolutePath());
                            }
                        }
                    }
                }
            }
        }

        Map<Class<?>, ServiceGenInfo> allService = findAllService(projClass);
        for (ServiceGenInfo info : allService.values()) {
            if (!genServiceProxy(info.serviceClass, targetPackageName, targetPath, false, null)) {
                throw new SysException("gen proxy file failed");
            }
            for (Class<?> modClass : info.serviceModClass) {
                if (!genServiceProxy(modClass, targetPackageName, targetPath, true, info.serviceClass)) {
                    throw new SysException("gen proxy file failed");
                }
            }
        }
    }

    public static Map<Class<?>, ServiceGenInfo> findAllService(Class<?> projClass) {
        Map<Class<?>, ServiceGenInfo> resultMap = new HashMap<>();

        Reflections reflections = new Reflections(
                new SubTypesScanner(false),
                projClass.getClassLoader(),
                projClass.getPackage().getName());

        for (Class<?> clazz : reflections.getSubTypesOf(Service.class)) {
            if (Modifier.isAbstract(clazz.getModifiers())) {
                continue;
            }
            ServiceGenInfo info = new ServiceGenInfo();
            info.serviceClass = clazz;
            resultMap.put(info.serviceClass, info);
        }
        for (Class<?> clazz : reflections.getSubTypesOf(ServiceMod.class)) {
            if (Modifier.isAbstract(clazz.getModifiers())) {
                continue;
            }
            Class<?> superGenericType = getSuperGenericType(clazz);
            if (superGenericType == null) {
                LogCore.gen.error("not find super generic type of" + clazz.getName());
                continue;
            }
            ServiceGenInfo serviceGenInfo = resultMap.get(superGenericType);
            if (serviceGenInfo != null) {
                serviceGenInfo.serviceModClass.add(clazz);
            } else {
                LogCore.gen.error("not find service of" + clazz.getName());
            }
        }
        return resultMap;
    }

    private static Class<?> getSuperGenericType(Class<?> clazz) {
        try {
            return (Class<?>) ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0];
        } catch (Exception e) {
            return null;
        }
    }


    public static boolean genServiceProxy(Class<?> serviceClass, String packageName, String targetPath, boolean asMod, Class<?> superGenericType) {
        String className = serviceClass.getSimpleName();
        String proxyClassName = serviceClass.getSimpleName() + SERVICE_PROXY_PREFIX;

        List<Object> methods = new ArrayList<>();
        Set<String> imports = new HashSet<>();

        Map<String, Object> rootMap = new HashMap<>();
        rootMap.put("className", className);
        rootMap.put("proxyClassName", proxyClassName);
        rootMap.put("packageName", packageName);
        rootMap.put("methods", methods);
        rootMap.put("imports", imports);

        if (asMod) {
            rootMap.put("modId", serviceClass.getSimpleName() + ".class.getName().hashCode()");
            rootMap.put("modMainClass", superGenericType.getSimpleName());
            imports.add(superGenericType.getCanonicalName());
        } else {
            rootMap.put("modId", String.valueOf(0));
            rootMap.put("modMainClass", serviceClass.getSimpleName());
        }


        imports.add(serviceClass.getCanonicalName());

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
            String methodString = getMethodString(method);

            Map<String, Object> methodInfo = new HashMap<>();
            methodInfo.put("name", method.getName());
            methodInfo.put("STR", methodString);
            methodInfo.put("code", String.valueOf(methodString.hashCode()));
            methodInfo.put("parameterCount", method.getParameterCount());
            methodInfo.put("parameterNames", getParameterNameString(method, ", ", ", "));
            methodInfo.put("parameterTypeNames", getParameterTypeNameString(method, ", ", ""));
            methodInfo.put("parameterTypeNameCasts", getParameterTypeNameCastString(method, ", ", ""));
            methodInfo.put("returnType", method.getGenericReturnType().getTypeName().replace('$', '.'));

            methods.add(methodInfo);
        }

        try {
            FreeMarkerUtils.genFile(GenTools.class.getClassLoader(), "templates", "ServiceProxy.ftl",
                    rootMap, targetPath, proxyClassName + ".java");
        } catch (Exception e) {
            LogCore.gen.error("gen proxy file error " + serviceClass.getName(), e);
            return false;
        }
        return true;
    }

    private static String getMethodString(Method method) {
        StringJoiner sj = new StringJoiner("_", "METHOD_KEY_", "");
        sj.add(method.getName());
        for (Class<?> clazz : method.getParameterTypes()) {
            sj.add(clazz.getSimpleName());
        }
        return sj.toString().toUpperCase();
    }

    private static String getParameterNameString(Method method, String delimiter, String suffix) {
        StringJoiner sj = new StringJoiner(delimiter, suffix, "");
        for (Parameter parameter : method.getParameters()) {
            sj.add(parameter.getName());
        }
        return sj.toString();
    }

    private static String getParameterTypeNameString(Method method, String delimiter, String suffix) {
        StringJoiner sj = new StringJoiner(delimiter, suffix, "");
        for (Parameter parameter : method.getParameters()) {
            sj.add(parameter.getType().getCanonicalName() + " " + parameter.getName());
        }
        return sj.toString();
    }

    private static String getParameterTypeNameCastString(Method method, String delimiter, String suffix) {
        StringJoiner sj = new StringJoiner(delimiter, suffix, "");
        for (Parameter parameter : method.getParameters()) {
            sj.add("(" + parameter.getType().getCanonicalName() + ") " + parameter.getName());
        }
        return sj.toString();
    }
}
