/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.model;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import mobi.meow.android.gootool.Logger;

/**
 * A display resolution. Immutable after construction.
 * Also has static methods to retrieve system supported resolutions.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id: Resolution.java 389 2010-05-02 18:03:02Z david $
 */
public class Resolution implements Comparable {
    private static final Logger log = Logger.getLogger(Resolution.class.getName());

    private final int width;
    private final int height;

    private Resolution(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isWidescreen() {
        return (height * 4) / 3 != width;
    }

    public String getAspectRatio() {
        int gcd = GCD(width, height);

        int widthFactor = width / gcd;
        int heightFactor = height / gcd;

        if (widthFactor == 5 && heightFactor == 3) {
            // Show 15:9 instead of 5:3
            gcd /= 3;
        } else if (widthFactor == 8 && heightFactor == 5) {
            // Show 16:10 instead of 8:5
            gcd /= 2;
        }

        return (width / gcd) + ":" + (height / gcd);
    }

    private static int GCD(int a, int b) {
        if (b == 0) return a;
        return GCD(b, a % b);
    }

    @Override
    public String toString() {

        String sb = width +
                "x" +
                height +

                // figure out aspect ratio

                " (" + getAspectRatio() + ")";
        return sb;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Resolution that = (Resolution) o;

        return height == that.height && width == that.width;
    }

    @Override
    public int hashCode() {
        int result;
        result = width;
        result = 31 * result + height;
        return result;
    }

    public int compareTo(Object o) {
        Resolution that = (Resolution) o;
        if (this.width < that.width)
            return -1;
        else if (this.width > that.width)
            return 1;
        else if (this.height < that.height)
            return -1;
        else if (this.height > that.height)
            return 1;
        else
            return 0;
    }


    private static final Set<Resolution> RESOLUTIONS;
    private static final Set<Integer> REFRESH_RATES;
    public static final Resolution DEFAULT_RESOLUTION;

    static {
        Set<Resolution> resolutions = new TreeSet<Resolution>();
        Set<Integer> refreshRates = new TreeSet<Integer>();

        // Make sure there's always a 800x600 resolution!
        resolutions.add(DEFAULT_RESOLUTION = new Resolution(800, 600));
/*
    for (GraphicsDevice screenDevice : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {

      for (DisplayMode displayMode : screenDevice.getDisplayModes()) {
        Resolution resolution = new Resolution(displayMode.getWidth(), displayMode.getHeight());
        resolutions.add(resolution);
        
        refreshRates.add(displayMode.getRefreshRate());
      }
    }*/

        RESOLUTIONS = Collections.unmodifiableSet(resolutions);
        REFRESH_RATES = Collections.unmodifiableSet(refreshRates);

        log.finer("System resolutions " + RESOLUTIONS);
        log.finer("Refresh rates " + REFRESH_RATES);
    }

    public static Set<Resolution> getSystemResolutions() {
        return RESOLUTIONS;
    }

    public static Resolution getResolutionByDimensions(int w, int h) {
        for (Resolution resolution : RESOLUTIONS) {
            if (resolution.getWidth() == w && resolution.getHeight() == h) {
                return resolution;
            }
        }
        return null;
    }

    public static Set<Integer> getSystemRefreshRates() {
        return REFRESH_RATES;
    }

    @SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardCodedStringLiteral"})
    public static void main(String[] args) {
        System.out.println("getSystemResolutions() = " + getSystemResolutions());
        System.out.println("getSystemRefreshRates() = " + getSystemRefreshRates());
    }
}
