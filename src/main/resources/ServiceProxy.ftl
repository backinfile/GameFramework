package ${packageName};

@SuppressWarnings("all")
public static class ${proxyClassName} extends ServiceProxyBase {
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
    private static final int ${m.STR} = 1;
</#list>


    static {
        Map<Integer, CommonFunction> methodMap = new HashMap<>();
<#list methods as m>
        methodMap.put(METHOD_KEY_GET_TEST_STRING, new CommonFunction(2, (Function2) ((service, value) -> {
            return ((Service1) service).getTestString((int) value);
        })));
        methodMap.put(METHOD_KEY_GET_TEST_STRING, new CommonFunction(2, (Function2) ((service, value) -> {
            return ((Service1) service).getTestString((int) value);
        })));
</#list>
        addMethodMap(TARGET_PORT_ID, methodMap);
    }

<#list methods as m>
    public Task<String> getTestString(int value) {
        return request(curPort, TARGET_PORT_ID, TARGET_OBJ_ID, METHOD_KEY_GET_TEST_STRING, value);
    }
</#list>
}