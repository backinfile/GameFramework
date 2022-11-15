package gen;

import com.backinfile.GameFramework.core.*;
import com.backinfile.support.func.*;
import test.ServiceTest.Service2;
import java.util.*;

@SuppressWarnings("all")
public class Service2Proxy extends ServiceProxyBase {
    public static final String TARGET_PORT_ID = Service2.class.getName();
    private static final long TARGET_OBJ_ID = 0L;

    private Port curPort;

    private Service2Proxy() {
    }

    public static Service2Proxy createInstance() {
        Service2Proxy proxy = new Service2Proxy();
        proxy.curPort = Port.getCurrentPort();
        return proxy;
    }

    public static Service2Proxy createInstance(boolean inMainThread) {
        if (!inMainThread) {
            return createInstance();
        }
        Service2Proxy proxy = new Service2Proxy();
        proxy.curPort = Node.getInstance().getPort(com.backinfile.GameFramework.service.MainThreadService.class.getName());
        return proxy;
    }


    static {
        Map<Integer, CommonFunction> methodMap = new HashMap<>();
        addMethodMap(TARGET_PORT_ID, methodMap);
    }

}