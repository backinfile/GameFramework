import com.backinfile.GameFramework.core.Node;
import com.backinfile.GameFramework.core.Port;
import com.backinfile.GameFramework.core.serialize.SerializableManager;
import com.backinfile.GameFramework.proxy.AsyncObject;
import com.backinfile.GameFramework.proxy.Proxy;
import com.backinfile.GameFramework.proxy.ProxyManager;
import com.backinfile.GameFramework.proxy.Task;
import com.backinfile.support.Time;
import com.backinfile.support.Utils;
import com.ea.async.Async;
import org.junit.jupiter.api.Test;

public class ProxyTest {

    public static class Port1 extends Port {
        public Port1() {
            super("Port1");
        }

        @Override
        public void startup() {
            timerQueue.applyTimer(Time.SEC, () -> {
                TmpAsyncObj tmpAsyncObj = Proxy.getProxy(TmpAsyncObj.class);
                tmpAsyncObj.testTask().whenComplete((v, ex) -> {
                    System.out.println("finish");
                });
            });
            super.startup();
        }
    }


    public static class TmpAsyncObj extends AsyncObject {
        public Task<Void> testTask() {
            System.out.println("in testTask " + this.getClass().getName());

            int value = Async.await(doSomething());
            assert value == 1243;

            ok = true;
            return Task.completedTask();
        }

        private Task<Integer> doSomething() {
            return Task.completedTask(1243);
        }
    }

    public static boolean ok = false;

    @Test
    public void testProxy() {
        Async.init();
        ProxyManager.registerAll(ProxyTest.class.getClassLoader());
        SerializableManager.registerAll(ProxyTest.class.getClassLoader());

        Node node = new Node();
        node.addPort(new Port1(), Port.of(new TmpAsyncObj()));
        node.startUp();
        node.waitAllPortStartupFinish();
        Utils.sleep(Time.SEC * 2);
        node.abort();
        node.join();

        assert ok;
    }

}
