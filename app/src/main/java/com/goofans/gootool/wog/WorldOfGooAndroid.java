/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */
package com.goofans.gootool.wog;

import static mobi.meow.android.gootool.MeowCatApplication.TAG;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import mobi.meow.android.gootool.AxmlModUtil;
import mobi.meow.android.gootool.IOUtils;
import mobi.meow.android.gootool.MeowCatApplication;
import mobi.meow.android.gootool.WoGInitData;

/**
 * Android implementation of WorldOfGoo class, partially based on Linux/Windows implementations.
 */
public class WorldOfGooAndroid extends WorldOfGoo {
    private final Context context = WoGInitData.getContext();

    /**
     * File names
     */
    private final String ORIGINAL_EXTRACTED_NAME = "original_extracted";
    private final String MODDED_TEMP_NAME = "modded_temp";
    private final String RES_DIR_LOCATION_IN_APK = "assets";

    /**
     * Files
     */
    public final File DATA_DIR = context.getExternalFilesDir(null);

    //extracted APK files
    private final File ORIGINAL_EXTRACTED_DIR = new File(DATA_DIR, ORIGINAL_EXTRACTED_NAME);
    public final File TEMP_MODDED_DIR = new File(DATA_DIR, MODDED_TEMP_NAME);

    //resources locations
    private final File ORIGINAL_EXTRACTED_RES_DIR = new File(ORIGINAL_EXTRACTED_DIR, RES_DIR_LOCATION_IN_APK);
    private final File TEMP_MODDED_RES_DIR = new File(TEMP_MODDED_DIR, RES_DIR_LOCATION_IN_APK);

    public File WOG_APK_FILE = null;
    private final File LASTRUN_FILE = new File(DATA_DIR, "lastrun.txt");

    private final Map<String, String> lastRunData = new HashMap<>();

    @Override
    public void init() {
        try {
            this.loadOrCreateLastRun();
        } catch (IOException e) {
            Log.w(TAG, "couldn't read lastrun.txt", e);
        }
        makeSureDirectoryExists(ORIGINAL_EXTRACTED_DIR);
        makeSureDirectoryExists(TEMP_MODDED_DIR);

        if (MeowCatApplication.worldOfGooApp == null) {
            return;
        }

        Log.i(TAG, String.format("Found World of Goo apk in %s", MeowCatApplication.worldOfGooApp.sourceDir));
        WOG_APK_FILE = new File(MeowCatApplication.worldOfGooApp.sourceDir);
        if (Objects.equals(lastRunData.get("original_apk_extracted"), "true")) {
            return;
        }
        forceClean();
        lastRunData.put("original_apk_extracted", "true");
        saveLastRun();
    }

    private void forceClean() {
        WoGInitData.getProgressListener().beginStep("Extracting original APK", true);
        IOUtils.extractZip(WOG_APK_FILE, ORIGINAL_EXTRACTED_DIR, WoGInitData.getProgressListener());
        WoGInitData.getProgressListener().beginStep("Deleting old modded directory", true);
        IOUtils.deleteDirContent(TEMP_MODDED_DIR);

        WoGInitData.getProgressListener().beginStep("Copying original files to new directory", false);
        IOUtils.copyFilesExcept(ORIGINAL_EXTRACTED_DIR, TEMP_MODDED_DIR, "assets");

        AxmlModUtil.modifyFiles();
    }

    private void loadOrCreateLastRun() throws IOException {
        //default values. In case when reading lastrun fails - there are default values
        lastRunData.put("original_apk_extracted", "false");
        lastRunData.put("modded_temp_extracted", "false");

        if (!LASTRUN_FILE.exists()) {
            saveLastRun();
        }
        Map<String, String> m = new HashMap<>();
        try {
            BufferedReader r = new BufferedReader(new FileReader(LASTRUN_FILE));
            String l;
            while ((l = r.readLine()) != null) {
                String[] split = l.split("=");
                if (split.length != 2) {
                    Log.w(TAG, "Invalid line in lastrun.txt: " + l);
                    continue;
                }
                m.put(split[0].trim(), split[1].trim());
            }
        } catch (FileNotFoundException e) {
            Log.wtf(TAG, "Lastrun file doesn't exist");
        }
        lastRunData.clear();
        lastRunData.putAll(m);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void saveLastRun() {
        //create the file
        try {
            LASTRUN_FILE.createNewFile();
            try (PrintWriter pw = new PrintWriter(new FileOutputStream(LASTRUN_FILE))) {
                for (Map.Entry<String, String> e : lastRunData.entrySet()) {
                    pw.println(e.getKey() + "=" + e.getValue());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private boolean getLastrunBool(String val) {
        return Boolean.parseBoolean(lastRunData.get(val));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void makeSureDirectoryExists(File file) {
        if (!file.isDirectory()) {
            Log.w(TAG, file + " already exists and is a file. Deleting...");
            file.delete();
        }
        if (!file.exists()) {
            Log.i(TAG, file + " doesn't exist, creating...");
            file.mkdir();
        }
    }

    @Override
    public void init(File path) {
        throw new UnsupportedOperationException("Initializing with specified location isn's supported on android");
    }

    @Override
    public boolean isWogFound() {
        return WOG_APK_FILE != null && WOG_APK_FILE.exists();
    }

    @Override
    public boolean isCustomDirSet() {
        return false;
    }

    @Override
    public File getGameFile(String pathname) {
        return getAndroidGameFile(getWogDir(), pathname);
    }

    @Override
    public File getCustomGameFile(String pathname) {
        return getAndroidGameFile(getCustomDir(), pathname);
    }

    private File getAndroidGameFile(File loc, String filename) {
        if (filename.contains(".")) {
            if (filename.endsWith(".bin") || filename.endsWith(".xml")) {
                return new File(loc, filename.substring(0, filename.length() - 4) + ".mp3");
            } else //if(
            //filename.endsWith(".png") ||
            //filename.endsWith(".binltl") ||
            //filename.endsWith(".binltl64") ||
            //filename.endsWith(".ogg") ||
            //filename.endsWith(".txt"))
            {
                return new File(loc, filename + ".mp3");
            }// else {
            // throw new UnsupportedOperationException("Unknown file format: " + filename);
            // }
        }
        return new File(loc, filename);
    }

    @Override
    public File getOldAddinsDir() {
        return null;
    }

    @Override
    public void launch() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public File getWogDir() {
        //gootool won't find files in .zip...
        return ORIGINAL_EXTRACTED_RES_DIR;
    }

    @Override
    public void setCustomDir(File customDir) {
        throw new UnsupportedOperationException("You cannot set custom directory on android.");
    }

    @Override
    public File getCustomDir() {
        //this is the directory used by gootool code for installing mods.
        //each time mods are reinstalled - it's recreated from the scratch
        //after installing mods - thses files are compressed into .apk
        return TEMP_MODDED_RES_DIR;
    }

    @Override
    public boolean isFirstCustomBuild() {
        //this method is unused on android anyway
        return true;
    }

    public static WorldOfGooAndroid get() {
        return (WorldOfGooAndroid) WorldOfGoo.getTheInstance();
    }
}
