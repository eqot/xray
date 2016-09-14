package com.eqot.xray;

import com.eqot.xray.xray.Sample$Xray;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

@Xray(Sample.class)
public class SampleTest2 {
    @Test
    public void add() throws Exception {
        final Sample$Xray sample = new Sample$Xray();
        final int result = sample.add(1, 2);
        assertEquals(3, result);
    }
}
