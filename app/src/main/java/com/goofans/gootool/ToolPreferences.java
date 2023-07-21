/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool;

import android.content.Context;
import android.content.SharedPreferences;

import com.goofans.gootool.util.VersionSpec;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.prefs.BackingStoreException;

import mobi.meow.android.gootool.Logger;
import mobi.meow.android.gootool.WoGInitData;

/**
 * This is specifically for preferences that relate to GooTool's state (e.g. whether translator mode is enabled).
 * It is NOT for preferences that affect game building (such as "skip opening movie") - such things belong in Configuration and ConfigurationWriterTask.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id: ToolPreferences.java 389 2010-05-02 18:03:02Z david $
 */
public class ToolPreferences {
    private static final Logger log = Logger.getLogger(ToolPreferences.class.getName());

    private static final SharedPreferences PREFS = WoGInitData.getContext().getSharedPreferences("GootoolPrefs", Context.MODE_PRIVATE);

    private static final String PREF_GOOTOOL_ID = "gootool_random_id";
    private static final String PREF_IGNORE_UPDATE = "gootool_ignore_update";
    private static final String PREF_L10N_MODE = "gootool_l10n_enabled";
    private static final String PREF_MRU_ADDIN_DIR = "gootool_mru_addin_dir";
    private static final String PREF_MRU_TOWER_DIR = "gootool_mru_tower_dir";
    private static final String PREF_WINDOW_POSITION = "gootool_window_position";

    private static final String PREF_WOG_DIR = "wog_dir";
    private static final String PREF_CUSTOM_DIR = "custom_dir";

    private static final String PREF_GOOFANS_USERNAME = "goofans_username";
    private static final String PREF_GOOFANS_PASSWORD = "goofans_password";
    private static final String PREF_GOOFANS_LOGINOK = "goofans_loginok";

    private static final String PREF_BILLBOARDS_DISABLE = "billboard_disable";
    private static final String PREF_BILLBOARDS_LASTCHECK = "billboard_lastcheck";

    private static final String PREF_RATINGS = "ratings";
    private static final String RATINGS_SEPARATOR = "|";
    private static final String RATINGS_VALUE_SEPARATOR = "=";

    private ToolPreferences() {
    }

    public static synchronized String getGooToolId() {
        String id = PREFS.getString(PREF_GOOTOOL_ID, null);
        if (id != null) return id;

        // base64 converts each 3  bytes to 4 bytes, so we want to use a multiple of 3 bytes (24 bits).
        // 24 bytes gives us 192 bits, leaving a resulting string length 32
        Random r = new Random();

        byte[] idBytes = new byte[24];
        r.nextBytes(idBytes);

        id = new String(Base64.getEncoder().encode(idBytes));

        setString(PREF_GOOTOOL_ID, id);
        return id;
    }

    /**
     * Returns whether the user has chosen to ignore this update version.
     *
     * @param version The version to check.
     * @return true if the user is ignoring this version.
     */
    public static boolean isIgnoreUpdate(VersionSpec version) {
        log.finer("Is ignoring update? " + version);

        String ignoreVersion = PREFS.getString(PREF_IGNORE_UPDATE, null);
        log.finer("Current setting: " + ignoreVersion);

        return ignoreVersion != null && ignoreVersion.equals(version.toString());
    }

    /**
     * User does not want to be notified of this version's availability again.
     *
     * @param version the version to ignore
     */
    public static void setIgnoreUpdate(VersionSpec version) {
        log.fine("Ignoring update " + version);
        setString(PREF_IGNORE_UPDATE, version.toString());
    }

    public static boolean isL10nEnabled() {
        return PREFS.getBoolean(PREF_L10N_MODE, false);
    }

    public static void setL10nEnabled(boolean enabled) {
        setBoolean(PREF_L10N_MODE, enabled);
    }

    public static String getMruAddinDir() {
        return PREFS.getString(PREF_MRU_ADDIN_DIR, null);
    }

    public static void setMruAddinDir(String mruDir) {
        setString(PREF_MRU_ADDIN_DIR, mruDir);
    }

    public static String getMruTowerDir() {
        return PREFS.getString(PREF_MRU_TOWER_DIR, null);
    }

    public static void setMruTowerDir(String mruDir) {
        setString(PREF_MRU_TOWER_DIR, mruDir);
    }

    public static String getWindowPosition() {
        return PREFS.getString(PREF_WINDOW_POSITION, null);
    }

    public static void setWindowPosition(String windowPosition) {
        setString(PREF_WINDOW_POSITION, windowPosition);
    }

    public static String getWogDir() {
        return PREFS.getString(PREF_WOG_DIR, null);
    }

    public static void setWogDir(String wogDir) {
        setString(PREF_WOG_DIR, wogDir);
    }

    public static String getCustomDir() {
        return PREFS.getString(PREF_CUSTOM_DIR, null);
    }

    public static void setCustomDir(String customDir) {
        setString(PREF_CUSTOM_DIR, customDir);
    }

    public static String getGooFansUsername() {
        return PREFS.getString(PREF_GOOFANS_USERNAME, null);
    }

    public static void setGooFansUsername(String username) {
        setString(PREF_GOOFANS_USERNAME, username);
    }

    public static String getGooFansPassword() {
        String enc = PREFS.getString(PREF_GOOFANS_PASSWORD, null);
        if (enc == null) return null;

        return new String(Base64.getDecoder().decode(enc.getBytes(StandardCharsets.UTF_8)));
    }

    public static void setGooFansPassword(String password) {
        setString(PREF_GOOFANS_PASSWORD, new String(Base64.getEncoder().encode(password.getBytes())));
    }

    public static boolean isGooFansLoginOk() {
        return PREFS.getBoolean(PREF_GOOFANS_LOGINOK, false);
    }

    public static void setGooFansLoginOk(boolean ok) {
        setBoolean(PREF_GOOFANS_LOGINOK, ok);
    }

    public static boolean isBillboardDisable() {
        return PREFS.getBoolean(PREF_BILLBOARDS_DISABLE, false);
    }

    public static void setBillboardDisable(boolean disable) {
        setBoolean(PREF_BILLBOARDS_DISABLE, disable);
    }

    public static long getBillboardLastCheck() {
        return PREFS.getLong(PREF_BILLBOARDS_LASTCHECK, 0);
    }

    public static void setBillboardLastCheck(long lastCheck) {
        setLong(PREF_BILLBOARDS_LASTCHECK, lastCheck);
    }

    public static void setRatings(Map<String, Integer> ratings) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> rating : ratings.entrySet()) {
            if (sb.length() > 0) sb.append(RATINGS_SEPARATOR);
            sb.append(rating.getKey()).append(RATINGS_VALUE_SEPARATOR).append(rating.getValue());
        }
        setString(PREF_RATINGS, sb.toString());
    }

    public static Map<String, Integer> getRatings() {
        String ratingsReg = PREFS.getString(PREF_RATINGS, null);
        if (ratingsReg == null) {
            return new TreeMap<String, Integer>();
        }

        StringTokenizer tok = new StringTokenizer(ratingsReg, RATINGS_SEPARATOR);

        Map<String, Integer> ratings = new TreeMap<String, Integer>();

        while (tok.hasMoreTokens()) {
            StringTokenizer tok2 = new StringTokenizer(tok.nextToken(), RATINGS_VALUE_SEPARATOR);
            String addinId = tok2.nextToken();
            String vote = tok2.nextToken();

            ratings.put(addinId, Integer.valueOf(vote));
        }

        return ratings;
    }

    /**
     * Prints preferences to the specified output stream, hiding the user's GooFans password.
     * This method is useful for debugging.
     *
     * @param out an output stream.
     * @throws BackingStoreException if the BackingStore cannot be reacehd
     */
    public static void list(PrintStream out) throws BackingStoreException {
        for (Map.Entry<String, ?> pref : PREFS.getAll().entrySet()) {
            out.print(pref.getKey() + "=");
            if (pref.getKey().equals(PREF_GOOFANS_PASSWORD)) {
                out.println("[hidden]");
            } else {
                out.println(pref.getValue());
            }
        }
    }

    private static void setBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = PREFS.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    private static void setString(String key, String value) {
        SharedPreferences.Editor editor = PREFS.edit();
        editor.putString(key, value);
        editor.commit();
    }

    private static void setLong(String key, long value) {
        SharedPreferences.Editor editor = PREFS.edit();
        editor.putLong(key, value);
        editor.commit();
    }
}
