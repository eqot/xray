package com.eqot.xray;

@SuppressWarnings("ALL")
public class SampleException {
    private void throwException() throws Exception {
        throw new Exception();
    }

    private int throwIllegalArgumentException() throws IllegalArgumentException {
        throw new IllegalArgumentException("message");
    }
}
