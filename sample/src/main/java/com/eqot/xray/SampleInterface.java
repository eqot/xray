package com.eqot.xray;

public class SampleInterface implements SampleInterface1, SampleInterface2 {
    @Override
    public int addInterface1(int value0, int value1) {
        return value0 + value1;
    }

    @Override
    public int addInterface2(int value0, int value1) {
        return value0 + value1;
    }
}