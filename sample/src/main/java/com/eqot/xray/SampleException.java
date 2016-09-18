package com.eqot.xray;

public class SampleException {
    private void throwException() throws Exception {
        throw new Exception();
    }

    private int throwIllegalArgumentException() throws IllegalArgumentException {
        throw new IllegalArgumentException("message");
    }
}
