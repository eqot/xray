package com.eqot.xray;

public class SampleInner {
    public class PublicInnerClass {
        private int addInner(int value0, int value1) {
            return value0 + value1;
        }
    }

    private class PrivateInnerClass {
        private int addInner(int value0, int value1) {
            return value0 + value1;
        }
    }

    private final PublicInnerClass mPublicInnerClass = new PublicInnerClass();
    private final PrivateInnerClass mPrivateInnerClass = new PrivateInnerClass();

    private int addPublicInner(int value0, int value1) {
        return mPublicInnerClass.addInner(value0, value1);
    }

    private int addPrivateInner(int value0, int value1) {
        return mPrivateInnerClass.addInner(value0, value1);
    }
}
