package gen;

import com.backinfile.GameFramework.core.*;
import com.backinfile.support.func.*;
import test.ServiceModTest.Service1Mod;
import test.GenProxyTest.Service1;
import java.util.*;

@SuppressWarnings("all")
public class Service1ModProxy extends ServiceProxyBase {
    public static final String TARGET_PORT_ID = Service1.class.getName();
    private static final int TARGET_MOD_ID = Service1Mod.class.getName().hashCode();

    private Port curPort;

    private Service1ModProxy() {
    }

    public static Service1ModProxy createInstance() {
        Service1ModProxy proxy = new Service1ModProxy();
        proxy.curPort = Port.getCurrentPort();
        return proxy;
    }

    public static Service1ModProxy createInstance(boolean inMainThread) {
        if (!inMainThread) {
            return createInstance();
        }
        Service1ModProxy proxy = new Service1ModProxy();
        proxy.curPort = Node.getInstance().getPort(com.backinfile.GameFramework.service.MainThreadService.class.getName());
        return proxy;
    }

    private static final int METHOD_KEY_ADD_INT_INT = -1171993533;

    static {
        Map<Integer, CommonFunction> methodMap = new HashMap<>();
        methodMap.put(METHOD_KEY_ADD_INT_INT, new CommonFunction(3, (Function3) ((_service, a, b) -> {
            return ((Service1Mod) _service).add((int) a, (int) b);
        })));
        addMethodMap(TARGET_PORT_ID, TARGET_MOD_ID, methodMap);
    }

    /**
     * {@link Service1Mod#add(int a, int b)}
     */
    public com.backinfile.GameFramework.core.Task<java.lang.Integer> add(int a, int b) {
        return request(curPort, TARGET_PORT_ID, TARGET_MOD_ID, METHOD_KEY_ADD_INT_INT, a, b);
    }

}