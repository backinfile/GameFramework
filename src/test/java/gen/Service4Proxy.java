package gen;

import com.backinfile.GameFramework.core.*;
import com.backinfile.support.func.*;
import test.ServiceModTest.Service4;
import java.util.*;

@SuppressWarnings("all")
public class Service4Proxy extends ServiceProxyBase {
    public static final String TARGET_PORT_ID = Service4.class.getName();
    private static final int TARGET_MOD_ID = 0;

    private Port curPort;

    private Service4Proxy() {
    }

    public static Service4Proxy createInstance() {
        Service4Proxy proxy = new Service4Proxy();
        proxy.curPort = Port.getCurrentPort();
        return proxy;
    }

    public static Service4Proxy createInstance(boolean inMainThread) {
        if (!inMainThread) {
            return createInstance();
        }
        Service4Proxy proxy = new Service4Proxy();
        proxy.curPort = Node.getInstance().getPort(com.backinfile.GameFramework.service.MainThreadService.class.getName());
        return proxy;
    }


    static {
        Map<Integer, CommonFunction> methodMap = new HashMap<>();
        addMethodMap(TARGET_PORT_ID, TARGET_MOD_ID, methodMap);
    }

}