package com.eqot.xray;

import com.eqot.xray.xray.SampleInner$Xray;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

@Xray(SampleInner.class)
public class SampleInnerTest {
    @Test
    public void addPublicInner() throws Exception {
        final SampleInner$Xray sample = new SampleInner$Xray();
        final int result = sample.addPublicInner(1, 2);
        assertEquals(3, result);
    }
}