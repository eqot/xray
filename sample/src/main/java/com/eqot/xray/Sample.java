package com.eqot.xray;

import java.util.List;

@SuppressWarnings("ALL")
public class Sample {
    private int mValue;
    private String mString;

    public Sample() {
        mValue = 0;
    }

    public Sample(int value) {
        mValue = value;
    }

    public Sample(String string) {
        mString = string;
    }

    private int add(int value) {
        return mValue + value;
    }

    private int add(int value0, int value1) {
        return value0 + value1;
    }

    protected int addProtect(int value0, int value1) {
        return value0 + value1;
    }

    private int sub(int value0, int value1) {
        return value0 - value1;
    }

    private String add(String word0, String word1) {
        return word0 + word1;
    }

    private int sum(int[] values) {
        int result = 0;
        for (int value : values) {
            result += value;
        }

        return result;
    }

    private int sum(List<Integer> values) {
        int result = 0;
        for (int value : values) {
            result += value;
        }

        return result;
    }

    private boolean nop(boolean value) { return value; }
    private byte nop(byte value) { return value; }
    private short nop(short value) { return value; }
    private int nop(int value) { return value; }
    private long nop(long value) { return value; }
    private float nop(float value) { return value; }
    private double nop(double value) { return value; }
    private char nop(char value) { return value; }
    private Integer nop(Integer value) { return value; }
    private String nop(String value) { return value; }

    private void nopWithIntArg(int value) {}
    private int nop() { return 123; }

    private int[] nop(int[] values) {
        return values;
    }

    private List<Integer> nop(List<Integer> values) {
        return values;
    }
}
