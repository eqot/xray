package com.eqot.xray;

import com.eqot.xray.xray.SampleInner$Xray;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

@Xray(SampleInner.class)
public class SampleInnerTest {

//    @Xray(SampleInner.InnerClass.class)
//    public class InnerClass {
//    }

    @Test
    public void addInner() throws Exception {
        final SampleInner$Xray sample = new SampleInner$Xray();
        final int result = sample.addInner(1, 2);
        assertEquals(3, result);
    }
}