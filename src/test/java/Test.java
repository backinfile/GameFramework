import com.backinfile.GameFramework.core.Call;
import com.backinfile.GameFramework.core.serialize.InputStream;
import com.backinfile.GameFramework.core.serialize.SerializableManager;
import com.backinfile.GameFramework.event.EventBase;
import com.backinfile.GameFramework.event.EventEx;
import com.backinfile.GameFramework.event.EventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class Test {

    @org.junit.jupiter.api.Test
    public void test() {
        SerializableManager.registerAll();
        Call call = new Call();
        call.portId = 1;
        call.objId = 123;
        call.value = Arrays.asList(1,2,3);
        call.value2 = 99;
        call.intArr = new int[]{1,2};
        call.setPrivateValue(122);
        call.aBoolean = true;
        call.aBoolean2 = true;
        call.aDouble = 12.234d;
        call.aFloat = 324f;
        call.aByte = 123;


        Call clone = SerializableManager.clone(call);
        System.out.println();
    }

    public static class EventGo extends EventBase {
        public int value;
    }

    @EventListener(EventGo.class)
    public static void onEventGo(EventGo eventGo) {
        System.out.println("go go go");
    }

    @org.junit.jupiter.api.Test
    public void TestEvent() {
        EventEx.registerAll(Test.class.getClassLoader());

        EventEx.fire(new EventGo());
    }
}
