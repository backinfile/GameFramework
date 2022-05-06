package com.backinfile.GameFramework.core;

import com.backinfile.GameFramework.Log;
import com.backinfile.support.Utils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * 代码生成改为反射形式
 */
public class SerializableManager {
    private static final Map<Integer, Object> idSaves = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> T parseFromSerializeID(int id) {
        return (T) idSaves.get(id);
    }

    public static int getCommonSerializeID(Object obj) {
        if (obj instanceof Class) {
            return ((Class<?>) obj).getName().hashCode();
        }
        return obj.getClass().getName().hashCode();
    }

    public static void registerAll(ClassLoader... classLoaders) {
        Reflections reflections = new Reflections(new SubTypesScanner(false), SerializableManager.class.getClassLoader(), classLoaders);
        registerAllEnum(reflections);
        registerAllSerialize(reflections);
    }

    private static void registerAllSerialize(Reflections reflections) {

        Collection<Class<? extends ISerializable>> classes = reflections.getSubTypesOf(ISerializable.class);
        for (Class<?> clazz : classes) {
            if (Modifier.isAbstract(clazz.getModifiers()) || Modifier.isInterface(clazz.getModifiers())) {
                continue;
            }
            try {
                int id = getCommonSerializeID(clazz);
                Constructor<?> constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                idSaves.put(id, constructor);
//                Log.serialize.info("find class:{}", clazz.getSimpleName());
            } catch (Exception e) {
                Log.serialize.error(Utils.format("可能是ISerializable接口的实现{}没有空的构造函数", clazz.getSimpleName()), e);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private static void registerAllEnum(Reflections reflections) {
        Set<Class<? extends Enum>> classes = reflections.getSubTypesOf(Enum.class);
        for (Class<?> clazz : classes) {
            try {
                int id = getCommonSerializeID(clazz);
                Method method = clazz.getDeclaredMethod("values");
                Object value = method.invoke(null);
                idSaves.put(id, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
