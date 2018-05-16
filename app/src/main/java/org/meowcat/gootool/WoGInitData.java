/*
 * Copyright (c) 2013-2018 MeowCat Studio Powered by MlgmXyysd All Rights Reserved.
 */

package org.meowcat.gootool;

import android.content.Context;
import android.content.pm.PackageManager;

import com.goofans.gootool.util.*;

/**
 * A hack to allow passing data to WorldOfGooAndroid init() method...
 */
public class WoGInitData {
  private static Context context;
  private static PackageManager pman;
  private static ProgressListener pl;

  public static void setContext(Context c) {
    context = c;
  }

  public static void setPackageManager(PackageManager pm) {
    pman = pm;
  }

  public static void setProgressListener(ProgressListener listener) {
    pl = listener;
  }

  public static Context getContext() {
    return context;
  }

  public static PackageManager getPackageManager() {
    return pman;
  }

  public static ProgressListener getProgressListener() {
    return pl;
  }
}
