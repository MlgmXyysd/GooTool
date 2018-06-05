/*
 * Copyright (c) 2013-2018 MeowCat Studio Powered by MlgmXyysd All Rights Reserved.
 */

package org.meowcat.gootool;

/**
 * Utility class that should be used instead of assert.
 */
public final class Assert {
    public static void that(boolean expr) {
        that(expr, "");
    }

    public static void that(boolean expr, String message) {
        if (BuildConfig.DEBUG && !expr) {
            throw new AssertionError(message);
        }
    }
}
