package org.meowcat.gootool;

import junit.framework.TestCase;

public class UtilsTest extends TestCase {

    public void testFromLittleEndianChar() throws Exception {
        char test = (char) 0xF1F2;
        char expL = (char) 0xF2F1;

        assertEquals(expL, Utils.fromLittleEndian(test));
    }

    public void testFromLittleEndianShort() throws Exception {
        short test = (short) 0xF1F2;
        short expL = (short) 0xF2F1;

        assertEquals(expL, Utils.fromLittleEndian(test));
    }

    public void testFromLittleEndianInt() throws Exception {
        int test = 0xF1F2F3F4;
        int expL = 0xF4F3F2F1;

        assertEquals(expL, Utils.fromLittleEndian(test));
    }

    public void testFromLittleEndianLong() throws Exception {
        long test = 0xF1F2F3F4F5F6F7F8L;
        long expL = 0xF8F7F6F5F4F3F2F1L;

        assertEquals(expL, Utils.fromLittleEndian(test));
    }
}