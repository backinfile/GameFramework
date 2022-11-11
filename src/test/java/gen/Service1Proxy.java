package gen;

import com.backinfile.GameFramework.core.*;
import com.backinfile.support.func.*;
import test.GenProxyTest.Service1;
import java.util.*;

@SuppressWarnings("all")
public class Service1Proxy extends ServiceProxyBase {
    public static final String TARGET_PORT_ID = Service1.class.getName();
    private static final long TARGET_OBJ_ID = 0L;

    private final Port curPort;

    private Service1Proxy() {
        this.curPort = Port.getCurrentPort();
    }

    public static Service1Proxy createInstance() {
        return new Service1Proxy();
    }

    private static final int METHOD_KEY_GETTESTSTRING_INT = -938218869;
    private static final int METHOD_KEY_CALCINFO_TESTINFO = 852183454;

    static {
        Map<Integer, CommonFunction> methodMap = new HashMap<>();
        methodMap.put(METHOD_KEY_GETTESTSTRING_INT, new CommonFunction(2, (Function2) ((_service, value) -> {
            return ((Service1) _service).getTestString((int) value);
        })));
        methodMap.put(METHOD_KEY_CALCINFO_TESTINFO, new CommonFunction(2, (Function2) ((_service, info) -> {
            return ((Service1) _service).calcInfo((test.GenProxyTest.TestInfo) info);
        })));
        addMethodMap(TARGET_PORT_ID, methodMap);
    }

    /**
     * {@link Service1#getTestString(int value)}
     */
    public com.backinfile.GameFramework.core.Task<java.lang.String> getTestString(int value) {
        return request(curPort, TARGET_PORT_ID, TARGET_OBJ_ID, METHOD_KEY_GETTESTSTRING_INT, value);
    }

    /**
     * {@link Service1#calcInfo(test.GenProxyTest.TestInfo info)}
     */
    public com.backinfile.GameFramework.core.Task<test.GenProxyTest.TestInfo> calcInfo(test.GenProxyTest.TestInfo info) {
        return request(curPort, TARGET_PORT_ID, TARGET_OBJ_ID, METHOD_KEY_CALCINFO_TESTINFO, info);
    }

}