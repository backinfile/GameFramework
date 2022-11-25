package gen;

import com.backinfile.GameFramework.core.*;
import com.backinfile.support.func.*;
import test.EventTest.Test2Service;
import java.util.*;

@SuppressWarnings("all")
public class Test2ServiceProxy extends ServiceProxyBase {
    public static final String TARGET_PORT_ID = Test2Service.class.getName();
    private static final int TARGET_MOD_ID = 0;

    private Port curPort;

    private Test2ServiceProxy() {
    }

    public static Test2ServiceProxy createInstance() {
        Test2ServiceProxy proxy = new Test2ServiceProxy();
        proxy.curPort = Port.getCurrentPort();
        return proxy;
    }

    public static Test2ServiceProxy createInstance(boolean inMainThread) {
        if (!inMainThread) {
            return createInstance();
        }
        Test2ServiceProxy proxy = new Test2ServiceProxy();
        proxy.curPort = Node.getInstance().getPort(com.backinfile.GameFramework.service.MainThreadService.class.getName());
        return proxy;
    }


    static {
        Map<Integer, CommonFunction> methodMap = new HashMap<>();
        addMethodMap(TARGET_PORT_ID, TARGET_MOD_ID, methodMap);
    }

}