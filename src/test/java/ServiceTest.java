import com.backinfile.GameFramework.core.*;
import com.ea.async.Async;

public class ServiceTest {
    public static class Service1 extends Service {
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
            return Task.completedTask(this.getClass().getName());
        }
    }

    public static class Service2 extends Service {

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

    public static class Service1Proxy extends ServiceProxyBase {
        public static final String TARGET_PORT_ID = Service1.class.getName();
        private static final long TARGET_OBJ_ID = 0L;

        private final Port curPort;

        private Service1Proxy() {
            this.curPort = Port.getCurrentPort();
        }

        public static Service1Proxy createInstance() {
            return new Service1Proxy();
        }


        private static final int METHOD_KEY_GET_TEST_STRING = 1;


        public Task<String> getTestString(int value) {
            Object result = Async.await(request(curPort, TARGET_PORT_ID, TARGET_OBJ_ID, METHOD_KEY_GET_TEST_STRING, value));
            return Task.completedTask((String) result);
        }
    }
}
