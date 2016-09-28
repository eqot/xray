package com.eqot.xray;

import com.eqot.xray.xray.SampleInterface$Xray;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

@Xray(SampleInterface.class)
public class SampleInterfaceTest {
    @Test
    public void addInterface1() throws Exception {
        final SampleInterface$Xray sample = new SampleInterface$Xray();
        final int result = sample.addInterface1(1, 2);
        assertEquals(3, result);
    }

    @Test
    public void addInterface2() throws Exception {
        final SampleInterface$Xray sample = new SampleInterface$Xray();
        final int result = sample.addInterface2(1, 2);
        assertEquals(3, result);
    }
}