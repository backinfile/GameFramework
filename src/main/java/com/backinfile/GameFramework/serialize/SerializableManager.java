package com.backinfile.GameFramework.serialize;

import com.backinfile.GameFramework.LogCore;
import com.backinfile.GameFramework.db.DBEntity;
import com.backinfile.support.SysException;
import com.backinfile.support.Utils;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * 代码生成改为反射形式
 */
public class SerializableManager {
    private static final Map<Integer, Object> objectUnpackerMap = new HashMap<>();
    private static final Map<Integer, Object> objectPackerMap = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> T getUnpackerObj(int id) {
        return (T) objectUnpackerMap.get(id);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getPackerObj(int id) {
        return (T) objectPackerMap.get(id);
    }

    public static int getCommonSerializeID(Object obj) {
        if (obj instanceof Class) {
            return ((Class<?>) obj).getName().hashCode();
        }
        return obj.getClass().getName().hashCode();
    }

    private static boolean registered = false;

    public static void registerAll(List<String> packagePaths, List<ClassLoader> classLoaders) {
        if (registered) {
            return;
        }
        registered = true;

        Reflections reflections = new Reflections(
                new SubTypesScanner(false),
                new TypeAnnotationsScanner(),
                LogCore.class.getClassLoader(),
                LogCore.class.getPackage().getName(),
                packagePaths, classLoaders);
        registerAllEnum(reflections);
        registerAllSerialize(reflections);
    }

    public static <T> T clone(T obj) {
        if (obj == null || obj.getClass().getSuperclass() == Number.class || obj.getClass() == String.class) {
            return obj;
        }

        OutputStream outputStream = new OutputStream();
        outputStream.write(obj);
        outputStream.close();

        InputStream inputStream = new InputStream(outputStream.getBytes());
        return inputStream.read();
    }


    private static void registerAllSerialize(Reflections reflections) {

        int interCnt = 0;
        int autoCnt = 0;

        // 优先使用接口方式（DBEntity除外，DBEntity使用自动挂载方式）
        for (Class<?> clazz : reflections.getSubTypesOf(ISerializable.class)) {
            if (Modifier.isAbstract(clazz.getModifiers()) || Modifier.isInterface(clazz.getModifiers())) {
                continue;
            }
            if (clazz.isAnnotationPresent(DBEntity.class)) {
                continue;
            }
            try {
                int id = getCommonSerializeID(clazz);
                Constructor<?> constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                objectUnpackerMap.put(id, constructor);
//                Log.serialize.info("find class:{}", clazz.getSimpleName());
                interCnt++;
            } catch (Exception e) {
                LogCore.serialize.error(Utils.format("可能是ISerializable接口的实现{}没有空的构造函数", clazz.getSimpleName()), e);
            }
        }

        // 自动挂载方式
        ClassPool pool = ClassPool.getDefault();
        Set<Class<?>> autoClassSet = new HashSet<>();
        autoClassSet.addAll(reflections.getTypesAnnotatedWith(Serializable.class));
        autoClassSet.addAll(reflections.getTypesAnnotatedWith(DBEntity.class));

        // public方法的Lookup
        MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();

        for (Class<?> clazz : autoClassSet) {
            if (ISerializable.class.isAssignableFrom(clazz) && !clazz.isAnnotationPresent(DBEntity.class)) {
                continue;
            }
            try {
                String typeName = clazz.getName();
                String simpleName = clazz.getSimpleName();
                List<Field> fields = getSerializableFields(clazz);
                for (Field field : fields) {
                    if (!checkField(field)) {
                        throw new SysException("类型" + typeName + "的字段" + field.getName() + "不能序列化");
                    }
                }

                CtClass ctClass = pool.makeClass("com.backinfile.GameFramework.serialize.gen." + simpleName + "Serializer");
                {
                    String header = Utils.format("public static void writeTo({0} obj, {1} out)", typeName, OutputStream.class.getCanonicalName());
                    StringJoiner body = new StringJoiner("", "{", "}");
                    for (Field field : fields) {
                        body.add(getWriteObjString(field));
                    }
                    CtMethod method = CtMethod.make(header + body, ctClass);
                    ctClass.addMethod(method);
                }
                {
                    String header = Utils.format("public static {0} readFrom({1} in)", typeName, InputStream.class.getCanonicalName());
                    StringJoiner body = new StringJoiner("", "{", "}");
                    body.add(Utils.format("{0} obj = new {0}();\n", typeName));
                    for (Field field : fields) {
                        body.add(getReadObjString(field));
                    }
                    body.add(Utils.format("return obj;"));
                    CtMethod method = CtMethod.make(header + body, ctClass);
                    ctClass.addMethod(method);
                }

                Class<?> cls = ctClass.toClass();

                {
                    // writeTo
                    MethodType mt = MethodType.methodType(void.class, clazz, OutputStream.class);
                    MethodHandle handle = publicLookup.findStatic(cls, "writeTo", mt);
                    objectPackerMap.put(getCommonSerializeID(clazz), handle);
                }

                {
                    // readFrom
                    MethodType mt = MethodType.methodType(clazz, InputStream.class);
                    MethodHandle handle = publicLookup.findStatic(cls, "readFrom", mt);
                    objectUnpackerMap.put(getCommonSerializeID(clazz), handle);
                }

                autoCnt++;
            } catch (Exception e) {
                LogCore.serialize.error("build class error: " + clazz.getName(), e);
            }
        }
        LogCore.serialize.info("register serialize obj over interface cnt:{}, auto cnt:{}", interCnt, autoCnt);
    }

    private static List<Field> getSerializableFields(Class<?> clazz) {
        List<Field> result = new ArrayList<>();
        for (Field field : clazz.getFields()) {
            if (!Modifier.isPublic(field.getModifiers())) {
                continue;
            }
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            if (Modifier.isFinal(field.getModifiers())) {
                continue;
            }
            result.add(field);
        }
        return result;
    }

    private static void registerAllEnum(Reflections reflections) {
        int cnt = 0;
        for (Class<?> clazz : reflections.getSubTypesOf(Enum.class)) {
            if (Modifier.isAbstract(clazz.getModifiers())) {
                continue;
            }
            try {
                Method method = clazz.getMethod("values");
                method.setAccessible(true);
                Object value = method.invoke(null);
                objectUnpackerMap.put(getCommonSerializeID(clazz), value);
                cnt++;
            } catch (Throwable e) {
                LogCore.serialize.error("registerEnum error: " + clazz.getName());
            }
        }
        LogCore.serialize.info("register enum over cnt:{}", cnt);
    }

    public static boolean checkField(Field field) {
        Class<?> clazz = field.getType();
        if (clazz.isPrimitive() || clazz.isArray()) {
            return true;
        }
        if (clazz == String.class || Number.class.isAssignableFrom(clazz)) {
            return true;
        }
        if (List.class.isAssignableFrom(clazz) || Set.class.isAssignableFrom(clazz)
                || Map.class.isAssignableFrom(clazz) || Enum.class.isAssignableFrom(clazz)) {
            return true;
        }

        if (ISerializable.class.isAssignableFrom(clazz)) {
            return true;
        }
        if (clazz.isAnnotationPresent(Serializable.class) || clazz.isAnnotationPresent(DBEntity.class)) {
            return true;
        }
        return false;
    }

    private static String getWriteObjString(Field field) {
        String fieldName = field.getName();
        Class<?> clazz = field.getType();
        if (clazz == int.class) {
            return Utils.format("out.getPacker().packInt(obj.{0}); \n", fieldName);
        } else if (clazz == long.class) {
            return Utils.format("out.getPacker().packLong(obj.{0}); \n", fieldName);
        } else if (clazz == double.class) {
            return Utils.format("out.getPacker().packDouble(obj.{0}); \n", fieldName);
        } else if (clazz == float.class) {
            return Utils.format("out.getPacker().packFloat(obj.{0}); \n", fieldName);
        } else if (clazz == boolean.class) {
            return Utils.format("out.getPacker().packBoolean(obj.{0}); \n", fieldName);
        } else if (clazz == byte.class) {
            return Utils.format("out.getPacker().packByte(obj.{0}); \n", fieldName);
        }

        return Utils.format("out.write(obj.{0});\n", fieldName);
    }

    private static String getReadObjString(Field field) {
        String fieldName = field.getName();
        Class<?> clazz = field.getType();
        if (clazz == int.class) {
            return Utils.format("obj.{0} = in.getUnpacker().unpackInt(); \n", fieldName);
        } else if (clazz == long.class) {
            return Utils.format("obj.{0} = in.getUnpacker().unpackLong(); \n", fieldName);
        } else if (clazz == double.class) {
            return Utils.format("obj.{0} = in.getUnpacker().unpackDouble(); \n", fieldName);
        } else if (clazz == float.class) {
            return Utils.format("obj.{0} = in.getUnpacker().unpackFloat(); \n", fieldName);
        } else if (clazz == boolean.class) {
            return Utils.format("obj.{0} = in.getUnpacker().unpackBoolean(); \n", fieldName);
        } else if (clazz == byte.class) {
            return Utils.format("obj.{0} = in.getUnpacker().unpackByte(); \n", fieldName);
        }

        return Utils.format("obj.{0} = ({1}) in.read();\n", fieldName, clazz.getCanonicalName());
    }


}
