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
}
