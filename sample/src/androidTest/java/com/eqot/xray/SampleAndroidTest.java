package com.eqot.xray;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

@Xray(SampleAndroid.class)
public class SampleAndroidTest {
    private Context mContext;

    @Before
    public void setUp() throws Exception {
        mContext = InstrumentationRegistry.getTargetContext();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void nop() throws Exception {
        final SampleAndroid$Xray sample = new SampleAndroid$Xray(mContext);
        assertEquals("123", sample.nop("123"));
    }

    @Test
    public void nopWithContext() throws Exception {
        final SampleAndroid$Xray sample = new SampleAndroid$Xray(mContext);
        assertEquals("123", sample.nop(mContext, "123"));
    }
}