package ${packageName};

import com.backinfile.GameFramework.core.*;
import com.backinfile.support.func.*;
<#list imports as import>
import ${import};
</#list>
import java.util.*;

@SuppressWarnings("all")
public class ${proxyClassName} extends ServiceProxyBase {
    public static final String TARGET_PORT_ID = ${modMainClass}.class.getName();
    private static final int TARGET_MOD_ID = ${modId};

    private Port curPort;

    private ${proxyClassName}() {
    }

    public static ${proxyClassName} createInstance() {
        ${proxyClassName} proxy = new ${proxyClassName}();
        proxy.curPort = Port.getCurrentPort();
        return proxy;
    }

    public static ${proxyClassName} createInstance(boolean inMainThread) {
        if (!inMainThread) {
            return createInstance();
        }
        ${proxyClassName} proxy = new ${proxyClassName}();
        proxy.curPort = Node.getInstance().getPort(com.backinfile.GameFramework.service.MainThreadService.class.getName());
        return proxy;
    }

<#list methods as m>
    private static final int ${m.STR} = ${m.code};
</#list>

    static {
        Map<Integer, CommonFunction> methodMap = new HashMap<>();
<#list methods as m>
        methodMap.put(${m.STR}, new CommonFunction(${m.parameterCount + 1}, (Function${m.parameterCount + 1}) ((_service${m.parameterNames}) -> {
            return ((${className}) _service).${m.name}(${m.parameterTypeNameCasts});
        })));
</#list>
        addMethodMap(TARGET_PORT_ID, TARGET_MOD_ID, methodMap);
    }

<#list methods as m>
    /**
     * {@link ${className}#${m.name}(${m.parameterTypeNames})}
     */
    public ${m.returnType} ${m.name}(${m.parameterTypeNames}) {
        return request(curPort, TARGET_PORT_ID, TARGET_MOD_ID, ${m.STR}${m.parameterNames});
    }

</#list>
}