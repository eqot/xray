package com.eqot.xray;

import com.eqot.xray.Xray;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

@Xray(Sample.class)
public class SampleTest {
    @Test
    public void add() throws Exception {
        final Sample$Xray sample = new Sample$Xray();
        final int result = sample.add(1, 2);
        assertEquals(result, 3);
    }

    @Test
    public void sub() throws Exception {
        final Sample$Xray sample = new Sample$Xray();
        final int result = sample.sub(1, 2);
        assertEquals(result, -1);
    }
}