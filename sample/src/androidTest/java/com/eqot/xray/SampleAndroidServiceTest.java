package com.eqot.xray;

import android.app.Service;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ServiceTestCase;

import com.eqot.xray.xray.SampleAndroidService$Xray;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@Xray(SampleAndroidService.class)
@RunWith(AndroidJUnit4.class)
public class SampleAndroidServiceTest extends ServiceTestCase<SampleAndroidService$Xray> {
    private SampleAndroidService$Xray mService;

    public SampleAndroidServiceTest() {
        super(SampleAndroidService$Xray.class);
        setContext(InstrumentationRegistry.getTargetContext());
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();

        startService(new Intent(mContext, SampleAndroidService$Xray.class));
        mService = getService();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();

        mService = null;
    }

    @Test
    public void onInitialize() throws Exception {
        assertNotNull(mService);
        assertEquals(123, mService.mValue());
    }

    @Test
    public void onCreate() throws Exception {
        mService.onCreate();
        assertEquals((Integer) 123, mService.mUpdatedValue());
    }

    @Test
    public void onDestroy() throws Exception {
        mService.onCreate();
        mService.onDestroy();
        assertNull(mService.mUpdatedValue());
    }

    @Test
    public void onStartCommand() throws Exception {
        final Intent intent = new Intent(mContext, SampleAndroidService$Xray.class);

        mService.onStartCommand(intent, Service.START_FLAG_RETRY, -1);
        mService.onStartCommand(null, Service.START_FLAG_RETRY, -1);
    }

    @Test
    public void add() throws Exception {
        assertEquals(3, mService.add(1, 2));
    }
}
