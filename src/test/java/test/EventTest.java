package test;

import com.backinfile.GameFramework.event.EventBase;
import com.backinfile.GameFramework.event.EventEx;
import com.backinfile.GameFramework.event.EventListener;

import java.util.Collections;

public class EventTest {

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
        EventEx.registerAll(Collections.emptyList(), Collections.singletonList(SerializeTest.class.getClassLoader()));

        EventEx.fire(new EventGo(123));

        assert eventCatching;
    }
}
