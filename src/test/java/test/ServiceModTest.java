package test;

import com.backinfile.GameFramework.GameStartUp;
import com.backinfile.GameFramework.core.*;
import com.backinfile.support.Time;
import com.ea.async.Async;
import gen.Service1ModProxy;
import org.junit.jupiter.api.Test;

public class ServiceModTest {

    public static class Service1Mod extends ServiceMod<GenProxyTest.Service1> {
        @Override
        public void init() {
            assert getService().getClass() == GenProxyTest.Service1.class;
        }

        @Override
        public void pulse() {

        }

        @Override
        public void pulsePerSec() {

        }

        @RPCMethod
        public Task<Integer> add(int a, int b) {
            return Task.completedTask(a + b);
        }
    }

    public static class Service4 extends Service {

        @Override
        public void init() {
            getTimerQueue().applyTimer(Time.SEC, () -> {
                Task.run(() -> {
                    Service1ModProxy proxy = Service1ModProxy.createInstance();
                    int result = Async.await(proxy.add(1, 33));
                    assert 34 == result;
                    Node.getInstance().abort();
                    return Task.completedTask();
                });
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
    public void testServiceMod() {
        GameStartUp.initAll(ServiceTest.class);
//        GameStartUp.startUp(GameStartUp.createAllService(ServiceModTest.class));
        GameStartUp.startUp(new GenProxyTest.Service1().addServiceMod(new Service1Mod()), new Service4());
    }
}
