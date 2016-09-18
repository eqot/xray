package com.eqot.xray;

import android.content.Context;

@SuppressWarnings("ALL")
public class SampleAndroid {
    private final Context mContext;

    public SampleAndroid(Context context) {
        mContext = context;
    }

    private String nop(String value) { return value; }
    private String nop(Context context, String value) { return value; }
}
