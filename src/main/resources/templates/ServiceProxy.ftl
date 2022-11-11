package ${packageName};

import com.backinfile.GameFramework.core.*;
import com.backinfile.support.func.*;
<#list imports as import>
import ${import};
</#list>
import java.util.*;

@SuppressWarnings("all")
public class ${proxyClassName} extends ServiceProxyBase {
    public static final String TARGET_PORT_ID = ${className}.class.getName();
    private static final long TARGET_OBJ_ID = 0L;

    private final Port curPort;

    private ${proxyClassName}() {
        this.curPort = Port.getCurrentPort();
    }

    public static ${proxyClassName} createInstance() {
        return new ${proxyClassName}();
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
        addMethodMap(TARGET_PORT_ID, methodMap);
    }

<#list methods as m>
    /**
     * {@link ${className}#${m.name}(${m.parameterTypeNames})}
     */
    public ${m.returnType} ${m.name}(${m.parameterTypeNames}) {
        return request(curPort, TARGET_PORT_ID, TARGET_OBJ_ID, ${m.STR}${m.parameterNames});
    }

</#list>
}