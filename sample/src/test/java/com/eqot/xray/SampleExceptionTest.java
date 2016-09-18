package com.eqot.xray;

import com.eqot.xray.xray.SampleException$Xray;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.nullValue;

@Xray(SampleException.class)
public class SampleExceptionTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void throwException() throws Exception {
        final SampleException$Xray sample = new SampleException$Xray();

        thrown.expect(Exception.class);
        thrown.expectMessage(nullValue(String.class));

        sample.throwException();
    }

    @Test
    public void throwIllegalArgumentException() throws Exception {
        final SampleException$Xray sample = new SampleException$Xray();

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("message");

        sample.throwIllegalArgumentException();
    }
}