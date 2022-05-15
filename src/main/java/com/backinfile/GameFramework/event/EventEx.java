package com.backinfile.GameFramework.event;

import com.backinfile.GameFramework.Log;
import com.backinfile.GameFramework.core.serialize.SerializableManager;
import org.reflections.Reflections;
import org.reflections.scanners.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class EventEx {
    private static final Map<Class<? extends EventBase>, List<Listener>> listenerMap = new HashMap<>();

    private static class Listener {
        private Method method;
        private int priority;

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
                Log.event.error("error in fire Event " + eventBase.getClass().getName(), e);
            }
        }
    }

    @EventListener(EventBase.class)
    public static void registerAll(ClassLoader... classLoaders) {
        Reflections reflections = new Reflections(
                new MethodAnnotationsScanner(),
                EventEx.class.getClassLoader(),
                classLoaders);

        listenerMap.clear();
        for (Method method : reflections.getMethodsAnnotatedWith(EventListener.class)) {
            if (!Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            EventListener annotation = method.getAnnotation(EventListener.class);
            if (method.getParameterCount() != 1) {
                Log.event.warn("register {}.{} failed", method.getDeclaringClass().getName(), method.getName());
                continue;
            }
            if (method.getParameterTypes()[0] != annotation.value()) {
                Log.event.warn("register {}.{} failed", method.getDeclaringClass().getName(), method.getName());
                continue;
            }
            listenerMap.computeIfAbsent(annotation.value(), keu -> new ArrayList<>()).add(new Listener(method, annotation.priority()));
        }
        for (List<Listener> listeners : listenerMap.values()) {
            listeners.sort(Comparator.comparing(Listener::getPriority).reversed());
        }
    }
}
