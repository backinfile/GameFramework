package com.backinfile.GameFramework.core;

import com.backinfile.GameFramework.LogCore;
import com.backinfile.GameFramework.core.serialize.InputStream;
import com.backinfile.GameFramework.core.serialize.OutputStream;


@SuppressWarnings("unchecked")
public class Result implements IResult {

    private Object[] results = null;
    private String errorString = "";

    /**
     * 仅供序列化使用
     */
    public Result() {
    }

    public Result(Object[] values) {
        this.results = values;
    }


    @Override
    public <T> T getResult(String key) {
        if (results == null) {
            return null;
        }
        for (int i = 0; i < results.length - 1; i += 2) {
            Object first = results[i];
            Object second = results[i + 1];
            if (first instanceof String) {
                if (((String) first).equals(key)) {
                    return (T) second;
                }
            }
        }
        return null;
    }

    @Override
    public <T> T getResult(int index) {
        if (results == null) {
            return null;
        }
        if (index < 0 || index >= results.length) {
            LogCore.core.error("getResult", new ArrayIndexOutOfBoundsException(""));
            return null;
        }
        return (T) results[index];
    }

    @Override
    public String getErrorString() {
        return errorString;
    }

    public void setErrorString(String errorString) {
        this.errorString = errorString;
    }

    @Override
    public void writeTo(OutputStream out) {
        out.write(errorString);
        out.write(results);
    }

    @Override
    public void readFrom(InputStream in) {
        errorString = in.read();
        results = in.read();
    }
}
