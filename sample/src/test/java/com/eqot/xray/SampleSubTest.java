package com.eqot.xray;

import com.eqot.xray.xray.SampleSub$Xray;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

@Xray(SampleSub.class)
public class SampleSubTest {
    @Test
    public void field() throws Exception {
        final SampleSub$Xray sample = new SampleSub$Xray();
        final int result = sample.mValue();
        assertEquals(147, result);
    }

    @Test
    public void derivedField() throws Exception {
        final SampleSub$Xray sample = new SampleSub$Xray();
        final int result = sample.mPublicValue();
        assertEquals(789, result);
    }

    @Test
    public void add() throws Exception {
        final SampleSub$Xray sample = new SampleSub$Xray();
        final int result = sample.addProtect(1, 2);
        assertEquals(3, result);
    }
}