package com.eqot.xray;

public class Sample {
    private int mValue;

    public Sample() {
        mValue = 0;
    }

    public Sample(int value) {
        mValue = value;
    }

    private int add(int value) {
        return mValue + value;
    }

    private int add(int value0, int value1) {
        return value0 + value1;
    }

    private int sub(int value0, int value1) {
        return value0 - value1;
    }

    private String add(String word0, String word1) {
        return word0 + word1;
    }
}
