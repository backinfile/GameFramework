package gen;

import com.backinfile.GameFramework.core.*;
import com.backinfile.support.func.*;
import test.EventTest.TestService;
import java.util.*;

@SuppressWarnings("all")
public class TestServiceProxy extends ServiceProxyBase {
    public static final String TARGET_PORT_ID = TestService.class.getName();
    private static final long TARGET_OBJ_ID = 0L;

    private Port curPort;

    private TestServiceProxy() {
    }

    public static TestServiceProxy createInstance() {
        TestServiceProxy proxy = new TestServiceProxy();
        proxy.curPort = Port.getCurrentPort();
        return proxy;
    }

    public static TestServiceProxy createInstance(boolean inMainThread) {
        if (!inMainThread) {
            return createInstance();
        }
        TestServiceProxy proxy = new TestServiceProxy();
        proxy.curPort = Node.getInstance().getPort(com.backinfile.GameFramework.service.MainThreadService.class.getName());
        return proxy;
    }


    static {
        Map<Integer, CommonFunction> methodMap = new HashMap<>();
        addMethodMap(TARGET_PORT_ID, methodMap);
    }

}