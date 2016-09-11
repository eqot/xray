package com.eqot.xray;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

@Xray(Sample.class)
public class SampleTest {
    @Test
    public void addWithMember() throws Exception {
        final Sample$Xray sample = new Sample$Xray(1);
        final int result = sample.add(2);
        assertEquals(3, result);
    }

    @Test
    public void add() throws Exception {
        final Sample$Xray sample = new Sample$Xray();
        final int result = sample.add(1, 2);
        assertEquals(3, result);
    }

    @Test
    public void sub() throws Exception {
        final Sample$Xray sample = new Sample$Xray();
        final int result = sample.sub(1, 2);
        assertEquals(-1, result);
    }

    @Test
    public void addString() throws Exception {
        final Sample$Xray sample = new Sample$Xray();
        final String result = sample.add("12", "34");
        assertEquals("1234", result);
    }

    @Test
    public void nopBoolean() throws Exception {
        final Sample$Xray sample = new Sample$Xray();
        assertEquals(true, sample.nop(true));
    }

    @Test
    public void nopByte() throws Exception {
        final Sample$Xray sample = new Sample$Xray();
        assertEquals((byte) 123, sample.nop((byte) 123));
    }

    @Test
    public void nopShort() throws Exception {
        final Sample$Xray sample = new Sample$Xray();
        assertEquals((short) 123, sample.nop((short) 123));
    }

    @Test
    public void nopInt() throws Exception {
        final Sample$Xray sample = new Sample$Xray();
        assertEquals(123, sample.nop(123));
    }

    @Test
    public void nopLong() throws Exception {
        final Sample$Xray sample = new Sample$Xray();
        assertEquals(123L, sample.nop(123L));
    }

    @Test
    public void nopFloat() throws Exception {
        final Sample$Xray sample = new Sample$Xray();
        assertEquals(123.4f, sample.nop(123.4f));
    }

    @Test
    public void nopDouble() throws Exception {
        final Sample$Xray sample = new Sample$Xray();
        assertEquals(123.4d, sample.nop(123.4d));
    }

    @Test
    public void nopChar() throws Exception {
        final Sample$Xray sample = new Sample$Xray();
        assertEquals('1', sample.nop('1'));
    }

    @Test
    public void nopInteger() throws Exception {
        final Sample$Xray sample = new Sample$Xray();
        assertEquals((Integer) 123, sample.nop((Integer) 123));
    }

    @Test
    public void nopString() throws Exception {
        final Sample$Xray sample = new Sample$Xray();
        assertEquals("123", sample.nop("123"));
    }

    @Test
    public void nopVoid() throws Exception {
        final Sample$Xray sample = new Sample$Xray();
        assertEquals(123, sample.nop());
    }

    @Test
    public void nopNoReturn() throws Exception {
        final Sample$Xray sample = new Sample$Xray();
        sample.nopWithIntArg(123);
    }
}
