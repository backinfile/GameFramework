package com.backinfile.GameFramework.core;

import com.backinfile.GameFramework.core.serialize.Serializable;

import java.util.List;

@Serializable
public class Call{
    public int portId;
    public int objId;
    public List<Integer> value;
    public Integer value2;
    public int[] intArr;
    private int privateValue;
    public float aFloat;
    public double aDouble;
    public byte aByte;
    public boolean aBoolean;
    public Boolean aBoolean2;
    public long[] longs;
    public long aLong;

    public void setPrivateValue(int privateValue) {
        this.privateValue = privateValue;
    }
}
