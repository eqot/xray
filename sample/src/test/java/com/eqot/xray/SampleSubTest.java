package com.eqot.xray;

import com.eqot.xray.xray.SampleSub$Xray;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

@Xray(SampleSub.class)
public class SampleSubTest {
    @Test
    public void add() throws Exception {
        final SampleSub$Xray sample = new SampleSub$Xray();
        final int result = sample.addProtect(1, 2);
        assertEquals(3, result);
    }
}