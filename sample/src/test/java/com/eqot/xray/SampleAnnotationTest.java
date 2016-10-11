package com.eqot.xray;

import com.eqot.xray.xray.SampleAnnotation$Xray;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

@Xray(SampleAnnotation.class)
public class SampleAnnotationTest {
    @Test
    public void add() throws Exception {
        final SampleAnnotation$Xray sample = new SampleAnnotation$Xray();
        final Integer result = sample.add(1, 2);
        assertEquals((Integer) 3, result);
    }

    @Test
    public void addWithNull() throws Exception {
        final SampleAnnotation$Xray sample = new SampleAnnotation$Xray();
        assertNull(sample.add(1, null));
        assertNull(sample.add(null, 2));
    }
}