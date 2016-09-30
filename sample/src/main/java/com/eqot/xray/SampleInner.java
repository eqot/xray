package com.eqot.xray;

public class SampleInner {
    public class InnerClass {
        public int addInner(int value0, int value1) {
            return value0 + value1;
        }
    }

    private final InnerClass mInnerClass = new InnerClass();

    private int addInner(int value0, int value1) {
        return mInnerClass.addInner(value0, value1);
    }
}
