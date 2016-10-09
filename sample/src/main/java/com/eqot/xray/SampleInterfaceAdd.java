package com.eqot.xray;

public class SampleInterfaceAdd implements SampleInterface {
    private int mDelta;

    public int calculate(int value0, int value1) {
        return value0 + value1 + mDelta;
    }
}
