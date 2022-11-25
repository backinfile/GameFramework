package test;

import com.backinfile.GameFramework.GameStartUp;
import com.backinfile.GameFramework.LogCore;
import com.backinfile.GameFramework.core.Node;
import com.backinfile.GameFramework.core.Task;
import com.backinfile.support.Time;
import com.backinfile.support.Utils;
import com.ea.async.Async;
import gen.Service1Proxy;
import org.junit.jupiter.api.Test;

public class MainThreadTest {

    @Test
    public void test() {
        GameStartUp.initAll(MainThreadTest.class);
        GameStartUp.startUpUsingMainThread(new GenProxyTest.Service1());

        Node node = Node.getInstance();
        while (node.isAlive()) {
            if (!node.isAlive()) break;
            Utils.sleep(Time.SEC / 30);
            logicUpdate();
        }
    }

    private boolean send = false;

    private void logicUpdate() {
        Node.getInstance().mainThreadUpdate();


        if (!send) {
            send = true;
//            Service1Proxy proxy = Service1Proxy.createInstance(true);
//            proxy.getTestString(124).whenComplete((r, ex) -> {
//                LogCore.test.info("got result:{}", r);
//                Node.getInstance().abort();
//            });
            Task.run(() -> {
                Service1Proxy proxy = Service1Proxy.createInstance(true);
                String result = Async.await(proxy.getTestString(124));
                LogCore.test.info("got result:{}", result);

                assert result != null;
                assert Thread.currentThread() == GameStartUp.MainThread;

                Node.getInstance().abort();
                return Task.completedTask();
            });
        }
    }
}
