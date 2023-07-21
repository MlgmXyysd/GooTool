/*
 * Copyright (c) 2013-2018 MeowCat Studio Powered by MlgmXyysd All Rights Reserved.
 */

package mobi.meow.android.gootool;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;

public class MeowCatApplication extends Application {

    public static final String TAG = "GooTool";

    @SuppressLint("StaticFieldLeak")
    public static Context context;

    public static ApplicationInfo worldOfGooApp;

    @Override
    public void onCreate() {
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
        super.onCreate();
    }
}