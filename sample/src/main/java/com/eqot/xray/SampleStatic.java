package com.eqot.xray;

@SuppressWarnings("ALL")
public class SampleStatic {
    private static int mValue;

    private static int add(int value0, int value1) {
        return value0 + value1;
    }

    private static int addWithMember(int value) {
        return mValue + value;
    }
}
