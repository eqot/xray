package com.eqot.xray;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

@SuppressWarnings("ALL")
@Retention(SOURCE)
@Target({ TYPE, METHOD })
public @interface Xray {
    Class<?> value();
}
