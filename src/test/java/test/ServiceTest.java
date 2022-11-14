package test;

import com.backinfile.GameFramework.LogCore;
import com.backinfile.GameFramework.core.*;
import com.backinfile.GameFramework.serialize.SerializableManager;
import com.backinfile.support.Time;
import com.backinfile.support.func.CommonFunction;
import com.backinfile.support.func.Function2;
import com.ea.async.Async;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
                    Service1Proxy proxy = Service1Proxy.createInstance();
                    String result = Async.await(proxy.getTestString(124));
                    LogCore.core.info("get result:{}", result);
                    LogCore.core.info("time:{}", getTime());
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

    @SuppressWarnings("all")
    public static class Service1Proxy extends ServiceProxyBase {
        public static final String TARGET_PORT_ID = Service3.class.getName();
        private static final long TARGET_OBJ_ID = 0L;

        private final Port curPort;

        private Service1Proxy() {
            this.curPort = Port.getCurrentPort();
        }

        public static Service1Proxy createInstance() {
            return new Service1Proxy();
        }


        private static final int METHOD_KEY_GET_TEST_STRING = 1;


        static {
            Map<Integer, CommonFunction> methodMap = new HashMap<>();
            methodMap.put(METHOD_KEY_GET_TEST_STRING, new CommonFunction(2, (Function2) ((service, value) -> {
                return ((Service3) service).getTestString((int) value);
            })));
            addMethodMap(TARGET_PORT_ID, methodMap);
        }

        public Task<String> getTestString(int value) {
            return request(curPort, TARGET_PORT_ID, TARGET_OBJ_ID, METHOD_KEY_GET_TEST_STRING, value);
        }
    }

    @Test
    public void test() {
        Async.init();
        SerializableManager.registerAll(Collections.emptyList(), Collections.singletonList(ServiceTest.class.getClassLoader()));


        Node node = new Node();
        node.addPort(new Service3(), new Service2());
        node.startUp();
        node.waitAllPortStartupFinish();
//        Utils.sleep(Time.SEC * 5);
//        node.abort();
        node.join();
    }
}
