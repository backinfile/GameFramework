package com.backinfile.GameFramework.event;

import com.backinfile.GameFramework.LogCore;
import com.backinfile.GameFramework.core.Node;
import com.backinfile.GameFramework.core.Port;
import com.backinfile.GameFramework.service.DisableAsyncEventService;
import com.backinfile.support.Utils;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class EventEx {
    private static final Map<Class<? extends EventBase>, List<Listener>> listenerMap = new HashMap<>();

    private static class Listener {
        private final MethodHandle method;
        private final int priority;
        private final String asyncService;

        public Listener(MethodHandle method, int priority, String asyncService) {
            this.method = method;
            this.priority = priority;
            this.asyncService = asyncService;
        }

        public int getPriority() {
            return priority;
        }

        public MethodHandle getMethod() {
            return method;
        }
    }

    public static void fire(EventBase eventBase) {
        fire(eventBase, true);
    }

    public static void fire(EventBase eventBase, boolean allowAsyncEvent) {
        if (!listenerMap.containsKey(eventBase.getClass())) {
            return;
        }
        for (Listener listener : listenerMap.get(eventBase.getClass())) {
            MethodHandle method = listener.getMethod();
            if (Utils.isNullOrEmpty(listener.asyncService)) { // 同步监听
                try {
                    method.invoke(eventBase);
                } catch (Throwable e) {
                    LogCore.event.error("error in listener " + method.toString(), e);
                }
            } else if (allowAsyncEvent) {  // 异步监听
                Port port = Node.getInstance().getPort(listener.asyncService);
                if (port == null) {
                    LogCore.event.warn("没有找到service:{} 无法触发事件", listener.asyncService);
                    continue;
                }

                port.post(() -> {
                    try {
                        method.invoke(eventBase);
                    } catch (Throwable e) {
                        LogCore.event.error("error in async listener " + method.toString(), e);
                    }
                });
            }
        }
    }

    private static boolean registered = false;

    public static void registerAll(List<String> packagePaths, List<ClassLoader> classLoaders) {
        if (registered) {
            return;
        }
        registered = true;
        Reflections reflections = new Reflections(
                new MethodAnnotationsScanner(),
                LogCore.class.getClassLoader(),
                LogCore.class.getPackage().getName(),
                packagePaths, classLoaders);

        MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
        listenerMap.clear();
        for (Method method : reflections.getMethodsAnnotatedWith(EventListener.class)) {
            registerMethod(publicLookup, method);
        }
        for (List<Listener> listeners : listenerMap.values()) {
            listeners.sort(Comparator.comparing(Listener::getPriority).reversed());
        }
        LogCore.event.info("register event listener over cnt:{}", listenerMap.values().stream().mapToInt(List::size).sum());
    }

    private static void registerMethod(MethodHandles.Lookup publicLookup, Method method) {
        if (!Modifier.isStatic(method.getModifiers())) {
            return;
        }
        if (!Modifier.isPublic(method.getModifiers())) {
            return;
        }

        if (method.getParameterCount() != 1) {
            LogCore.event.warn("register {}#{} failed", method.getDeclaringClass().getName(), method.getName());
            return;
        }
        Class<?> parameterType = method.getParameterTypes()[0];
        if (Modifier.isAbstract(parameterType.getModifiers())) {
            LogCore.event.warn("register {}#{} failed", method.getDeclaringClass().getName(), method.getName());
            return;
        }
        if (!EventBase.class.isAssignableFrom(parameterType)) {
            LogCore.event.warn("register {}#{} failed", method.getDeclaringClass().getName(), method.getName());
            return;
        }
        @SuppressWarnings("unchecked")
        Class<? extends EventBase> eventClass = (Class<? extends EventBase>) parameterType;

        EventListener annotation = method.getAnnotation(EventListener.class);
        int priority = annotation.priority();

        String targetService = annotation.async() == DisableAsyncEventService.class ? "" : annotation.async().getName();

        try {
            MethodType mt = MethodType.methodType(void.class, eventClass);
            MethodHandle handle = publicLookup.findStatic(method.getDeclaringClass(), method.getName(), mt);

            listenerMap.computeIfAbsent(eventClass, key -> new ArrayList<>()).add(new Listener(handle, priority, targetService));
        } catch (Throwable e) {
            LogCore.event.error("error in find method handle", e);
        }
    }
}
