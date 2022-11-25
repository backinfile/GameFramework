package gen;

import com.backinfile.GameFramework.core.*;
import com.backinfile.support.func.*;
import test.ServiceTest.Service3;
import java.util.*;

@SuppressWarnings("all")
public class Service3Proxy extends ServiceProxyBase {
    public static final String TARGET_PORT_ID = Service3.class.getName();
    private static final int TARGET_MOD_ID = 0;

    private Port curPort;

    private Service3Proxy() {
    }

    public static Service3Proxy createInstance() {
        Service3Proxy proxy = new Service3Proxy();
        proxy.curPort = Port.getCurrentPort();
        return proxy;
    }

    public static Service3Proxy createInstance(boolean inMainThread) {
        if (!inMainThread) {
            return createInstance();
        }
        Service3Proxy proxy = new Service3Proxy();
        proxy.curPort = Node.getInstance().getPort(com.backinfile.GameFramework.service.MainThreadService.class.getName());
        return proxy;
    }

    private static final int METHOD_KEY_GETTESTSTRING_INT = -938218869;

    static {
        Map<Integer, CommonFunction> methodMap = new HashMap<>();
        methodMap.put(METHOD_KEY_GETTESTSTRING_INT, new CommonFunction(2, (Function2) ((_service, value) -> {
            return ((Service3) _service).getTestString((int) value);
        })));
        addMethodMap(TARGET_PORT_ID, TARGET_MOD_ID, methodMap);
    }

    /**
     * {@link Service3#getTestString(int value)}
     */
    public com.backinfile.GameFramework.core.Task<java.lang.String> getTestString(int value) {
        return request(curPort, TARGET_PORT_ID, TARGET_MOD_ID, METHOD_KEY_GETTESTSTRING_INT, value);
    }

}