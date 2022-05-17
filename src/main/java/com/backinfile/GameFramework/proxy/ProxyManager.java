package com.backinfile.GameFramework.proxy;

import com.backinfile.GameFramework.LogCore;
import com.backinfile.GameFramework.core.Call;
import com.backinfile.GameFramework.core.CallPoint;
import com.backinfile.GameFramework.core.Node;
import com.backinfile.GameFramework.core.Port;
import com.backinfile.GameFramework.event.EventEx;
import com.backinfile.support.SysException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

public class ProxyManager {

    static final Map<Class<?>, Constructor<?>> proxyClassMap = new HashMap<>();
    static final Map<Class<?>, String> targetPortMap = new HashMap<>();
    static final Map<Class<?>, Map<Integer, Method>> proxyClassMethodMap = new HashMap<>();

    // 推送请求
    public static Task<Object> request(String targetPort, long targetObjId, int methodKey, Object[] args) {
        Port port = Port.getCurrentPort();
        if (port == null) {
            if (Thread.currentThread() == Node.MainThread) {
                // TODO 主线程也可发送
            }
            return Task.failure(new SysException("rpc not request from a port!"));
        }
        port.getTerminal().sendNewCall(new CallPoint(targetPort, targetObjId), methodKey, args);
        Call lastOutCall = port.getTerminal().getLastOutCall();
        Task<Object> task = new Task<>();
        port.getTerminal().listenOutCall(lastOutCall.id, ir -> {
            if (ir.hasError()) {
                task.completeExceptionally(new SysException("rpc return error:" + ir.getErrorString()));
            } else {
                task.complete(ir.getResult());
            }
        });
        return task;
    }

    // 处理请求
    @SuppressWarnings("unchecked")
    public static void handleRequest(Port port, Call call) {
        AsyncObject asyncObj = port.getAsyncObj(call.to.objId);
        if (asyncObj == null) {
            LogCore.core.error("not find AsyncObject of " + call);
            return;
        }
        Map<Integer, Method> methodMap = proxyClassMethodMap.get(asyncObj.getClass());
        if (methodMap == null || !methodMap.containsKey(call.method)) {
            LogCore.core.error("not find method of " + asyncObj.getClass().getName() + " in " + call);
            return;
        }
        Method method = methodMap.get(call.method);
        try {
            Task<Object> task = (Task<Object>) method.invoke(asyncObj, call.args);
            task.whenComplete((obj, ex) -> {
                if (ex != null) {
                    Call callReturn = call.newErrorReturn(ex.getMessage());
                    Node.Instance.handleCall(callReturn);
                    LogCore.core.warn("error in invoke origin method " + method, ex);
                } else {
                    Call callReturn = call.newCallReturn(new Object[]{obj});
                    Node.Instance.handleCall(callReturn);
                }
            });
        } catch (Exception e) {
            LogCore.core.error("error in invoke origin method " + method, e);
        }
    }

    public static void registerPortId(Class<? extends AsyncObject> clazz, String targetPort) {
        if (!ProxyManager.targetPortMap.containsKey(clazz)) {
            ProxyManager.targetPortMap.put(clazz, targetPort);
        }
    }

    private static boolean registered = false;

    public static void registerAll(ClassLoader... classLoaders) {
        if (registered) {
            return;
        }
        registered = true;

        Reflections reflections = new Reflections(
                new SubTypesScanner(false),
                EventEx.class.getClassLoader(),
                classLoaders);


        ClassPool pool = ClassPool.getDefault();
        for (Class<?> clazz : reflections.getSubTypesOf(AsyncObject.class)) {
            if (Modifier.isAbstract(clazz.getModifiers())) {
                continue;
            }

            CtClass ctClass = null;
            try {
                ctClass = pool.get(clazz.getName());
            } catch (Exception e) {
                LogCore.core.error("error in get CtClass of " + clazz.getName(), e);
                continue;
            }
            CtClass newClass = pool.makeClass("com.backinfile.GameFramework.proxy.gen." + clazz.getSimpleName() + "Proxy", ctClass);


            for (Method method : clazz.getDeclaredMethods()) {
                try {
                    if (!Modifier.isPublic(method.getModifiers())) {
                        continue;
                    }
                    if (Modifier.isStatic(method.getModifiers())) {
                        continue;
                    }
                    if (method.getReturnType() != Task.class) {
                        continue;
                    }
                    CtMethod superMethod = ctClass.getDeclaredMethod(method.getName());
                    CtMethod overrideMethod = CtNewMethod.delegator(superMethod, newClass);
                    int methodKey = getMethodKey(method);
                    overrideMethod.setBody(
                            String.format("{return %s.request(this.targetPort, this.getObjId(), %d, $args);}",
                                    ProxyManager.class.getCanonicalName(), methodKey));
                    newClass.addMethod(overrideMethod);


//                    Log.core.info("replace {}", superMethod.getName());

                    proxyClassMethodMap.computeIfAbsent(clazz, key -> new HashMap<>()).put(methodKey, method);
                } catch (Exception e) {
                    LogCore.core.error("error in replace method content of " + clazz.getName(), e);
                }
            }

            try {
                Class<?> proxyClass = newClass.toClass();
                Constructor<?> declaredConstructor = null;
                try {
                    declaredConstructor = proxyClass.getConstructor(long.class);
                } catch (Exception ignored) {
                }
                if (declaredConstructor == null) {
                    declaredConstructor = proxyClass.getConstructor();
                }
                proxyClassMap.put(clazz, declaredConstructor);
            } catch (Exception e) {
                LogCore.core.error("error in get constructor of " + clazz.getName(), e);
            }

        }
    }

    private static String getCtMethodHead(Method method) {
        StringBuilder sb = new StringBuilder();
        sb.append("public ");
        sb.append(Task.class.getCanonicalName());
        sb.append(" ");
        sb.append(method.getName());

        StringJoiner sj = new StringJoiner(",", "(", ")");
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> type = parameterTypes[i];
            sj.add(type.getCanonicalName() + " arg" + i);
        }

        sb.append(sj);
        return sb.toString();
    }

    private static int getMethodKey(Method method) {
        return Objects.hash(method.toString());
    }
}
