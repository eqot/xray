package com.eqot.xray;

import android.content.Intent;
import android.test.ServiceTestCase;

import org.junit.Test;

public class SampleAndroidServiceTest extends ServiceTestCase<SampleAndroidService> {
    public SampleAndroidServiceTest() {
        super(SampleAndroidService.class);
    }

    @Test
    public void testStartService() throws Exception {
        final Intent startIntent = new Intent();
        startIntent.setClass(getContext(), SampleAndroidService.class);
        startService(startIntent);

        final SampleAndroidService service = getService();
        assertNotNull(service);
    }
}