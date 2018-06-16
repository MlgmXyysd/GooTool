/*
 * Copyright (c) 2013-2018 MeowCat Studio Powered by MlgmXyysd All Rights Reserved.
 */

package org.meowcat.gootool;

import android.support.annotation.NonNull;

import java.util.Arrays;

/**
 * Little-Endian UTF16 encoded null terminated length prefixed CharSequence.
 * This format us used internally in Android binary XML format.
 */
public class LeUtf16String implements CharSequence {

    private final char[] rawData;

    LeUtf16String(char[] rawData) {
        if (rawData.length < 2) {
            throw new IllegalArgumentException("Not enough data");
        }
        if (rawData[rawData.length - 1] != 0) {
            throw new IllegalArgumentException("Thsi sequence is not sull terminated");
        }

        char[] c = new char[rawData.length];
        System.arraycopy(rawData, 0, c, 0, rawData.length);
        this.rawData = c;
    }

    LeUtf16String(CharSequence seq) {
        char[] rawData = new char[seq.length() + 2];

        int l = seq.length();
        if (l > 0xFFFF) {
            throw new IllegalArgumentException("Too long CharSequence. Max supported length: " + 0xFFFF);
        }
        rawData[0] = (char) (l << 8 | l >>> 8);

        for (int i = 0; i < l; i++) {
            char a = seq.charAt(i);
            rawData[i + 1] = (char) (a << 8 | a >>> 8);
        }
        this.rawData = rawData;
    }

    @Override
    public int length() {
        return this.rawData.length - 2;
    }

    @Override
    public char charAt(int index) {
        if (index < 0 || index > this.length()) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
        char l = rawData[index + 1];
        return (char) (l >>> 8 | l << 8);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        if (start > end || start < 0 || end < 0 || start > this.length() || end > this.length()) {
            throw new IndexOutOfBoundsException(start + ", " + end);
        }
        int len = end - start;
        if (len > 0xFFFF) {
            throw new IllegalArgumentException("Too long LeUtf16String length. Max is " + 0xFFFF);
        }
        char encLen = (char) (len & 0xFF << 8 | len >> 8);
        char[] newSeq = new char[len + 2];
        newSeq[0] = encLen;
        System.arraycopy(this.rawData, 1 + start, newSeq, 1, encLen - 1);
        return new LeUtf16String(newSeq);
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.length());
        for (int i = 0; i < this.length(); i++) {
            sb.append(this.charAt(i));
        }
        return sb.toString();
    }

    public char[] getRawData() {
        char[] c = new char[rawData.length];
        System.arraycopy(rawData, 0, c, 0, rawData.length);
        return c;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LeUtf16String that = (LeUtf16String) o;

        return Arrays.equals(this.rawData, that.rawData);

    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(rawData);
    }
}
