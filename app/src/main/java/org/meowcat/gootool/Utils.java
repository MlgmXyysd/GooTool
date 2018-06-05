/*
 * Copyright (c) 2013-2018 MeowCat Studio Powered by MlgmXyysd All Rights Reserved.
 */

package org.meowcat.gootool;

public class Utils {
    public static char fromLittleEndian(char c) {
        return (char) fromLittleEndian(c & 0xFFFFL, 2);
    }

    public static short fromLittleEndian(short s) {
        return (short) fromLittleEndian(s & 0xFFFFL, 2);
    }

    public static int fromLittleEndian(int i) {
        return (int) fromLittleEndian(i & 0xFFFFFFFFL, 4);
    }

    public static long fromLittleEndian(long l) {
        return fromLittleEndian(l, 8);
    }

    private static long fromLittleEndian(long l, int numBytes) {
        long result = 0;
        for (int i = 0; i < numBytes; i++) {
            result |= ((l >>> 8 * i) & 0xFFL) << (8 * (numBytes - 1 - i));
        }
        return result;
    }
}
