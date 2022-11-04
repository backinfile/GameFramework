package com.backinfile.GameFramework.core;

import com.backinfile.support.func.CommonFunction;

import java.util.HashMap;
import java.util.Map;

public class MethodHub {
    private final Map<Integer, CommonFunction> methodMap = new HashMap<>();

    public CommonFunction get(int methodKey) {
        return methodMap.get(methodKey);
    }

    public Map<Integer, CommonFunction> getMethodMap() {
        return methodMap;
    }
}
