package test;

import com.backinfile.GameFramework.GameStartUp;
import com.backinfile.GameFramework.LogCore;
import com.backinfile.GameFramework.core.Node;
import com.backinfile.GameFramework.core.Port;
import com.backinfile.GameFramework.core.Service;
import com.backinfile.GameFramework.event.EventBase;
import com.backinfile.GameFramework.event.EventEx;
import com.backinfile.GameFramework.event.EventListener;
import com.backinfile.support.Time;
import org.junit.jupiter.api.Test;

public class EventTest {

    public static class EventGo extends EventBase {
        public final int value;

        public EventGo(int value) {
            this.value = value;
        }
    }

    @EventListener
    public static void onEventGo(EventGo eventGo) {
        eventCatching = true;
        assert eventGo.value == 123;
    }

    private static boolean eventCatching = false;

    @org.junit.jupiter.api.Test
    public void testEvent() {
        GameStartUp.initAll(EventTest.class);

        EventEx.fire(new EventGo(123));

        assert eventCatching;
    }


    public static class EventAsyncTest extends EventBase {
        private final int value;

        public EventAsyncTest(int value) {
            this.value = value;
        }
    }

    @EventListener(async = TestService.class)
    public static void onAsyncEvent(EventAsyncTest event) {
        asyncEventCatch = true;
        assert event.value == 123;
        assert Port.getCurrentPort().getClass() == TestService.class;
        LogCore.test.info("catch asyncEvent");
    }

    @EventListener
    public static void onSyncEvent2(EventAsyncTest event) {
        asyncEventCatch2 = true;
        assert event.value == 123;
        assert Port.getCurrentPort().getClass() == Test2Service.class;
        LogCore.test.info("catch asyncEvent2");
    }

    private static boolean asyncEventCatch = false;
    private static boolean asyncEventCatch2 = false;

    private static class TestService extends Service {
        @Override
        public void init() {
        }

        @Override
        public void pulse() {

        }

        @Override
        public void pulsePerSec() {

        }
    }

    private static class Test2Service extends Service {
        @Override
        public void init() {
            post(() -> {
                EventEx.fire(new EventAsyncTest(123));
                getTimerQueue().applyTimer(Time.SEC * 2, Node.getInstance()::abort);
            });
        }

        @Override
        public void pulse() {

        }

        @Override
        public void pulsePerSec() {

        }
    }

    @Test
    public void testAsyncEvent() {
        GameStartUp.initAll(EventTest.class);
        GameStartUp.startUp(TestService::new, Test2Service::new);

        assert asyncEventCatch;
        assert asyncEventCatch2;
    }
}
