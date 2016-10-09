package com.eqot.xray;

import com.eqot.xray.xray.SampleInterfaceAdd$Xray;
import com.eqot.xray.xray.SampleInterfaceCore$Xray;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

@Xray(SampleInterfaceCore.class)
public class SampleInterfaceCoreTest {
    @Xray(SampleInterfaceAdd.class)
    public class Dummy {}

    @Test
    public void addInterface() throws Exception {
        final SampleInterfaceAdd$Xray add = new SampleInterfaceAdd$Xray();
        add.mDelta(3);

        final SampleInterfaceCore$Xray core = new SampleInterfaceCore$Xray();
        core.set(add);
        final int result = core.calculate(1, 2);
        assertEquals(6, result);
    }
}
