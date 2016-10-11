package com.eqot.xray;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class SampleAnnotation {
    @NonNull private Integer mNonNullValue = 123;
    @Nullable private Integer mNullableValue;

    @Nullable
    public Integer add(@Nullable Integer value0, @Nullable Integer value1) {
        if (value0 == null || value1 == null) {
            return null;
        }

        return value0 + value1;
    }
}
