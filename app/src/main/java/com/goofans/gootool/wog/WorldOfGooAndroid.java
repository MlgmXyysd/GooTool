/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */
package com.goofans.gootool.wog;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import org.meowcat.gootool.AxmlModUtil;
import org.meowcat.gootool.WoGInitData;
import org.meowcat.gootool.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import static org.meowcat.gootool.MainActivity.*;

/**
 * Android implementation of WorldOfGoo class, partially based on Linux/Windows implementations.
 */
public class WorldOfGooAndroid extends WorldOfGoo {
  private Context context = WoGInitData.getContext();

  /**
   * File names
   */
  public final String ORIGINAL_EXTRACTED_NAME = "original_extracted";
  public final String MODDED_TEMP_NAME = "modded_temp";
  public final String RES_DIR_LOCATION_IN_APK = "assets";

  /**
   * Package names
   */
  public final String WOG_PACKAGE_NAME = "com.twodboy.worldofgoofull";

  /**
   * Files
   */
  public final File DATA_DIR = context.getExternalFilesDir(null);

  //extracted APK files
  public final File ORIGINAL_EXTRACTED_DIR = new File(DATA_DIR, ORIGINAL_EXTRACTED_NAME);
  public final File TEMP_MODDED_DIR = new File(DATA_DIR, MODDED_TEMP_NAME);

  //resources locations
  public final File ORIGINAL_EXTRACTED_RES_DIR = new File(ORIGINAL_EXTRACTED_DIR, RES_DIR_LOCATION_IN_APK);
  public final File TEMP_MODDED_RES_DIR = new File(TEMP_MODDED_DIR, RES_DIR_LOCATION_IN_APK);

  public File WOG_APK_FILE;
  private final File LASTRUN_FILE = new File(DATA_DIR, "lastrun.txt");

  private boolean isWogFound = false;

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

    PackageManager pm = WoGInitData.getPackageManager();

    String originalLocation = null;
    for (ApplicationInfo app : pm.getInstalledApplications(0)) {
      if (app.packageName.equals(WOG_PACKAGE_NAME)) {
        Log.i(TAG, String.format("Found World of Goo apk in %s", app.sourceDir));
        originalLocation = app.sourceDir;
        WOG_APK_FILE = new File(originalLocation);
        isWogFound = true;
      }
    }
    if (!isWogFound) {
      Log.i(TAG, "World of Goo apk not found. Is it installed?");
      return;
    }
    if(lastRunData.get("original_apk_extracted").equals("true")) {
      return;
    }
    forceClean();
    lastRunData.put("original_apk_extracted", "true");
    saveLastRun();
  }

  public void forceClean() {
    WoGInitData.getProgressListener().beginStep("Extracting original APK", true);
    IOUtils.extractZip(WOG_APK_FILE, ORIGINAL_EXTRACTED_DIR, WoGInitData.getProgressListener());
    WoGInitData.getProgressListener().beginStep("Deleting old modded directory", false);
    IOUtils.deleteDirContent(TEMP_MODDED_DIR);

    WoGInitData.getProgressListener().beginStep("Copying original files to new directory", false);
    IOUtils.copyFilesExcept(ORIGINAL_EXTRACTED_DIR, TEMP_MODDED_DIR, "assets");

    AxmlModUtil.modifyFiles();
  }

  private void loadOrCreateLastRun() throws IOException {
    //default values. In case when reading lastrun fails - there are default values
    lastRunData.put("original_apk_extracted", "false");
    lastRunData.put("modded_temp_extracted", "false");

    if(!LASTRUN_FILE.exists()) {
      saveLastRun();
    }
    Map<String,String> m = new HashMap<>();
    try {
      BufferedReader r = new BufferedReader(new FileReader(LASTRUN_FILE));
      String l;
      while((l = r.readLine()) != null) {
        String[] split = l.split("=");
        if(split.length != 2) {
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

  private void saveLastRun() {
    //create the file
    try {
      LASTRUN_FILE.createNewFile();
      PrintWriter pw = null;
      try {
        pw = new PrintWriter(new FileOutputStream(LASTRUN_FILE));
        for (Map.Entry<String, String> e : lastRunData.entrySet()) {
          pw.println(e.getKey() + "=" + e.getValue());
        }
      }finally {
        if(pw != null)
          pw.close();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }
  private boolean getLastrunBool(String val) {
    return Boolean.parseBoolean(lastRunData.get(val));
  }

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
  public void init(File path) throws FileNotFoundException {
    throw new UnsupportedOperationException("Initializing with specified location isn's supported on android");
  }

  @Override
  public boolean isWogFound() {
    return isWogFound;
  }

  @Override
  public boolean isCustomDirSet() {
    return false;
  }

  @Override
  public File getGameFile(String pathname) throws IOException {
    return getAndroidGameFile(getWogDir(), pathname);
  }

  @Override
  public File getCustomGameFile(String pathname) throws IOException {
    return getAndroidGameFile(getCustomDir(), pathname);
  }

  private File getAndroidGameFile(File loc, String filename) {
    //TODO: actually check if it's a file
    if(filename.contains(".")) {
      if(filename.endsWith(".bin") || filename.endsWith(".xml")) {
        return new File(loc, filename.substring(0, filename.length() - 4) + ".mp3");
      } else if(filename.endsWith(".png") ||
              filename.endsWith(".binltl") ||
              filename.endsWith(".binltl64") ||
              filename.endsWith(".ogg") ||
              filename.endsWith(".txt")) {
        return new File(loc, filename + ".mp3");
      } else {
        throw new UnsupportedOperationException("Unknown file format: " + filename);
      }
    }
    return new File(loc, filename);
  }

  @Override
  public File getOldAddinsDir() {
    return null;
  }

  @Override
  public void launch() throws IOException {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public File getWogDir() throws IOException {
    //gootool won't find files in .zip...
    return ORIGINAL_EXTRACTED_RES_DIR;
  }

  @Override
  public void setCustomDir(File customDir) throws IOException {
    throw new UnsupportedOperationException("You cannot set custom directory on android.");
  }

  @Override
  public File getCustomDir() throws IOException {
    //this is the directory used by gootool code for installing mods.
    //each time mods are reinstalled - it's recreated from the scratch
    //after installing mods - thses files are compressed into .apk
    return TEMP_MODDED_RES_DIR;
  }

  @Override
  public boolean isFirstCustomBuild() throws IOException {
    //this method is unused on android anyway
    return true;
  }

  public static final WorldOfGooAndroid get() {
    return (WorldOfGooAndroid) WorldOfGoo.getTheInstance();
  }
}
