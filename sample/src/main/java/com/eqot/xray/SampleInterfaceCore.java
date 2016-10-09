package com.eqot.xray;

public class SampleInterfaceCore {
    private SampleInterface mSampleInterface;

    private void set(SampleInterface sampleInterface) {
        mSampleInterface = sampleInterface;
    }

    private int calculate(int value0, int value1) {
        return mSampleInterface.calculate(value0, value1);
    }
}
