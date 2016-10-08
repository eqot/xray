package com.eqot.xray;

import android.content.Intent;
import android.test.ServiceTestCase;

import com.eqot.xray.xray.SampleAndroidService$Xray;

import org.junit.Test;

@Xray(SampleAndroidService.class)
public class SampleAndroidServiceTest extends ServiceTestCase<SampleAndroidService$Xray> {
    public SampleAndroidServiceTest() {
        super(SampleAndroidService$Xray.class);
    }

    @Test
    public void testStartService() throws Exception {
        final Intent startIntent = new Intent();
        startIntent.setClass(getContext(), SampleAndroidService$Xray.class);
        startService(startIntent);

        final SampleAndroidService$Xray service = getService();
        assertNotNull(service);
    }

    @Test
    public void testServiceField() throws Exception {
        final Intent startIntent = new Intent();
        startIntent.setClass(getContext(), SampleAndroidService$Xray.class);
        startService(startIntent);

        final SampleAndroidService$Xray service = getService();
        assertEquals(123, service.mValue());
    }
}