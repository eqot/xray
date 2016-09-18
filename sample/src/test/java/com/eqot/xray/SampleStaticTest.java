package com.eqot.xray;

import com.eqot.xray.xray.SampleStatic$Xray;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

@Xray(SampleStatic.class)
public class SampleStaticTest {
    @Test
    public void add() throws Exception {
        final int result = SampleStatic$Xray.add(1, 2);
        assertEquals(3, result);
    }

    @Test
    public void addWithMember() throws Exception {
        SampleStatic$Xray.mValue(1);
        final int result = SampleStatic$Xray.addWithMember(2);
        assertEquals(3, result);
    }

    @Test
    public void getMember() throws Exception {
        SampleStatic$Xray.mValue(123);
        final int result = SampleStatic$Xray.mValue();
        assertEquals(123, result);
    }
}
