import com.backinfile.GameFramework.async.Task;
import com.backinfile.GameFramework.core.Node;
import com.backinfile.GameFramework.core.Port;
import com.backinfile.GameFramework.core.serialize.SerializableManager;
import com.backinfile.GameFramework.proxy.AsyncObject;
import com.backinfile.GameFramework.proxy.Proxy;
import com.backinfile.GameFramework.proxy.ProxyManager;
import com.backinfile.support.Time2;
import com.backinfile.support.timer.TimerQueue;
import org.junit.jupiter.api.Test;

public class ProxyTest {

    public static class Port1 extends Port {
        private TimerQueue timerQueue = new TimerQueue();

        public Port1() {
            super("Port1");
        }

        @Override
        public void startup() {
            timerQueue.applyTimer(Time2.SEC * 2, () -> {
                TmpAsyncObj tmpAsyncObj = Proxy.getProxy(TmpAsyncObj.class);
                tmpAsyncObj.testTask().whenComplete((v, ex) -> {
                    System.out.println("finish");
                });
            });
        }

        @Override
        public void pulse() {
            timerQueue.update();
        }
    }

    public static class Port2 extends Port {
        public Port2() {
            super("Port2");
        }

        @Override
        public void startup() {
            add(new TmpAsyncObj());
        }
    }

    public static class TmpAsyncObj extends AsyncObject {
        public Task<Void> testTask() {
            System.out.println("in testTask " + this.getClass().getName());
            return Task.completedTask();
        }
    }

    @Test
    public void testProxy() {
        ProxyManager.init(ProxyTest.class.getClassLoader());
        SerializableManager.registerAll(ProxyTest.class.getClassLoader());

        Node node = new Node();
        node.addPort(new Port1());
        node.addPort(new Port2());
        node.startUp();
        node.join();
    }

}
