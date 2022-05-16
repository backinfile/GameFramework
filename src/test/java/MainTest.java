import com.backinfile.GameFramework.core.serialize.*;
import com.backinfile.GameFramework.event.EventBase;
import com.backinfile.GameFramework.event.EventEx;
import com.backinfile.GameFramework.event.EventListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainTest {

    @Serializable
    public static class SerializableTestClass1 {
        public long aLong;
    }

    @Serializable
    public static class SerializableTestClass2 {
        public int anInt;
        public float aFloat;
        public int[] ints;
        public List<Integer> integers;
        public boolean aBoolean;
        public Map<Integer, Integer> map;
        public Integer integer;
        public SerializableTestClass1 serializableTestClass1;
        public SerializableTestClass3 serializableTestClass3;
    }

    public static class SerializableTestClass3 implements ISerializable {
        public int value;

        @Override
        public void writeTo(OutputStream out) {
            out.write(value);
        }

        @Override
        public void readFrom(InputStream in) {
            value = in.read();
        }
    }

    @org.junit.jupiter.api.Test
    public void testSerializable() {
        SerializableManager.registerAll();
        SerializableTestClass2 obj = new SerializableTestClass2();
        obj.anInt = 1243;
        obj.aFloat = 1.234f;
        obj.ints = new int[]{1, 2, 3};
        obj.integers = Arrays.asList(1, 2, 3, 4);
        obj.aBoolean = true;
        obj.map = new HashMap<>();
        obj.serializableTestClass1 = new SerializableTestClass1();
        obj.serializableTestClass1.aLong = 234;
        obj.integer = 4324;
        obj.serializableTestClass3 = new SerializableTestClass3();
        obj.serializableTestClass3.value = 4535;


        SerializableTestClass2 clone = SerializableManager.clone(obj);


        assert obj != clone;
        assert obj.anInt == clone.anInt;
        assert obj.aFloat == clone.aFloat;
        assert Arrays.equals(obj.ints, clone.ints);
        assert obj.integers.equals(clone.integers);
        assert obj.aBoolean == clone.aBoolean;
        assert clone.map != null;
        assert obj.serializableTestClass1.aLong == clone.serializableTestClass1.aLong;
        assert obj.integer.equals(clone.integer);
        assert obj.serializableTestClass3.value == clone.serializableTestClass3.value;
    }

    public static class EventGo extends EventBase {
        public int value;

        public EventGo(int value) {
            this.value = value;
        }
    }

    @EventListener(EventGo.class)
    public static void onEventGo(EventGo eventGo) {
        eventCatching = true;
        assert eventGo.value == 123;
    }

    private static boolean eventCatching = false;

    @org.junit.jupiter.api.Test
    public void testEvent() {
        EventEx.registerAll(MainTest.class.getClassLoader());

        EventEx.fire(new EventGo(123));

        assert eventCatching;
    }
}
