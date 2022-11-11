package com.backinfile.GameFramework.event;

import com.backinfile.GameFramework.LogCore;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class EventEx {
    private static final Map<Class<? extends EventBase>, List<Listener>> listenerMap = new HashMap<>();

    private static class Listener {
        private final Method method;
        private final int priority;

        public Listener(Method method, int priority) {
            this.method = method;
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }

        public Method getMethod() {
            return method;
        }
    }

    public static void fire(EventBase eventBase) {
        if (!listenerMap.containsKey(eventBase.getClass())) {
            return;
        }
        for (Listener listener : listenerMap.get(eventBase.getClass())) {
            try {
                listener.getMethod().invoke(null, eventBase);
            } catch (Exception e) {
                LogCore.event.error("error in fire Event " + eventBase.getClass().getName(), e);
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

        listenerMap.clear();
        for (Method method : reflections.getMethodsAnnotatedWith(EventListener.class)) {
            if (!Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            EventListener annotation = method.getAnnotation(EventListener.class);
            if (Modifier.isAbstract(annotation.value().getModifiers())) {
                LogCore.event.warn("register {}.{} failed", method.getDeclaringClass().getName(), method.getName());
                continue;
            }
            if (method.getParameterCount() != 1) {
                LogCore.event.warn("register {}.{} failed", method.getDeclaringClass().getName(), method.getName());
                continue;
            }
            if (method.getParameterTypes()[0] != annotation.value()) {
                LogCore.event.warn("register {}.{} failed", method.getDeclaringClass().getName(), method.getName());
                continue;
            }
            listenerMap.computeIfAbsent(annotation.value(), key -> new ArrayList<>()).add(new Listener(method, annotation.priority()));
        }
        for (List<Listener> listeners : listenerMap.values()) {
            listeners.sort(Comparator.comparing(Listener::getPriority).reversed());
        }
        LogCore.event.info("register event listener over cnt:{}", listenerMap.values().stream().mapToInt(List::size).sum());
    }
}
