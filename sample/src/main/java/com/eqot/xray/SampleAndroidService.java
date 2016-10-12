package com.eqot.xray;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;


public class SampleAndroidService extends Service {
    @SuppressWarnings("unused")
    private static final String TAG = SampleAndroidService.class.getSimpleName();

    private int mValue = 123;
    private Integer mUpdatedValue;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mUpdatedValue = 123;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mUpdatedValue = null;
    }

    private int add(int value0, int value1) {
        return value0 + value1;
    }
}
