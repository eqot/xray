package com.eqot.xray;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

@Xray(Sample.class)
public class SampleTest {
    @Test
    public void addWithMember() throws Exception {
        final Sample$Xray sample = new Sample$Xray(1);
        final int result = sample.add(2);
        assertEquals(3, result);
    }

    @Test
    public void add() throws Exception {
        final Sample$Xray sample = new Sample$Xray();
        final int result = sample.add(1, 2);
        assertEquals(3, result);
    }

    @Test
    public void sub() throws Exception {
        final Sample$Xray sample = new Sample$Xray();
        final int result = sample.sub(1, 2);
        assertEquals(-1, result);
    }

    @Test
    public void addString() throws Exception {
        final Sample$Xray sample = new Sample$Xray();
        final String result = sample.add("12", "34");
        assertEquals("1234", result);
    }
}
