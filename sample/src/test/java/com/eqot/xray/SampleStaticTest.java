package com.eqot.xray;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

@Xray(SampleStatic.class)
public class SampleStaticTest {
    @Test
    public void add() throws Exception {
        final int result = SampleStatic$Xray.add(1, 2);
        assertEquals(3, result);
    }
}