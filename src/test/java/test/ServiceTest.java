package test;

import com.backinfile.GameFramework.GameStartUp;
import com.backinfile.GameFramework.LogCore;
import com.backinfile.GameFramework.core.Node;
import com.backinfile.GameFramework.core.RPCMethod;
import com.backinfile.GameFramework.core.Service;
import com.backinfile.GameFramework.core.Task;
import com.backinfile.support.Time;
import com.ea.async.Async;
import gen.Service3Proxy;
import org.junit.jupiter.api.Test;

public class ServiceTest {
    public static class Service3 extends Service {
        @Override
        public void init() {

        }

        @Override
        public void pulse() {

        }

        @Override
        public void pulsePerSec() {

        }

        @RPCMethod
        public Task<String> getTestString(int value) {
            LogCore.core.info("req value:{}", value);
            return Task.completedTask(this.getClass().getName());
        }
    }

    public static class Service2 extends Service {

        @Override
        public void init() {
            LogCore.core.info("time:{}", getTime());
            getTimerQueue().applyTimer(Time.SEC, () -> {
                Task.run(() -> {
                    Service3Proxy proxy = Service3Proxy.createInstance();
                    String result = Async.await(proxy.getTestString(124));
                    LogCore.core.info("get result:{}", result);
                    LogCore.core.info("time:{}", getTime());
                    assert result != null;
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
    public void test() {
        GameStartUp.initAll(ServiceTest.class);
        GameStartUp.startUp(Service2::new, Service3::new);
    }
}
