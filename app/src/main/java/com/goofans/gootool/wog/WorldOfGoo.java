/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.wog;

import android.content.Context;
import android.content.SharedPreferences;

import com.goofans.gootool.addins.Addin;
import com.goofans.gootool.addins.AddinFactory;
import com.goofans.gootool.addins.AddinFormatException;
import com.goofans.gootool.model.Configuration;
import com.goofans.gootool.model.Language;
import com.goofans.gootool.model.Resolution;
import com.goofans.gootool.platform.PlatformSupport;
import com.goofans.gootool.util.Utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;

import mobi.meow.android.gootool.DuplicateAddinException;
import mobi.meow.android.gootool.Logger;
import mobi.meow.android.gootool.WoGInitData;

/**
 * Encapsulates the static data about World of Goo, i.e. path and version.
 * World of Goo doesn't have any registry entries, so we have to guess at common locations.
 * It also doesn't have any version data that I can find, so we use the file mtime to determine the version.
 * // orig version exe 2,191,360
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id: WorldOfGoo.java 406 2010-06-16 21:22:57Z david $
 */
public abstract class WorldOfGoo {
    private static final Logger log = Logger.getLogger(WorldOfGoo.class.getName());

    private static final String GOOMOD_EXTENSION = "goomod";
    private static final String GOOMOD_EXTENSION_WITH_DOT = "." + GOOMOD_EXTENSION;
    private static final String USER_CONFIG_FILE = "properties/config.txt";


    private static final WorldOfGoo theInstance = new WorldOfGooAndroid();

    public static List<Addin> availableAddins = new LinkedList<>();

    // TODO these should move into a new Preferences class
    static final String PREF_LASTVERSION = "gootool_version";
    static final String PREF_ALLOW_WIDESCREEN = "allow_widescreen";
    static final String PREF_SKIP_OPENING_MOVIE = "skip_opening_movie";
    static final String PREF_WATERMARK = "watermark";
    static final String PREF_LANGUAGE = "language";
    static final String PREF_SCREENWIDTH = "screen_width";
    static final String PREF_SCREENHEIGHT = "screen_height";
    static final String PREF_REFRESHRATE = "refresh_rate";
    static final String PREF_UIINSET = "ui_inset";
    static final String PREF_ADDINS = "addins";
    static final String PREF_WINDOWS_VOLUME_CONTROL = "windows_volume_control";

    private static final String STORAGE_DIR_ADDINS = "addins";

    protected WorldOfGoo() {
    }

    public static WorldOfGoo getTheInstance() {
        return theInstance;
    }

    public abstract void init();

    public abstract void init(File path) throws FileNotFoundException;

    public abstract boolean isWogFound();

    public abstract boolean isCustomDirSet();

    private SharedPreferences getPreferences() {
        return WoGInitData.getContext().getSharedPreferences("GootoolPrefs", Context.MODE_PRIVATE);
    }

    public abstract File getGameFile(String pathname) throws IOException;

    public abstract File getCustomGameFile(String pathname) throws IOException;

    /**
     * This returns the directory that GooTool stored installed addins in prior to version 1.1.
     *
     * @return The addin directory, or null if it doesn't exist.
     */
    public abstract File getOldAddinsDir();

    /**
     * Returns the new directory that GooTool stores installed addins from version 1.1 onward.
     *
     * @return The addin directory.
     * @throws IOException if the addin directory couldn't be determined or created.
     */
    private File getAddinsDir() throws IOException {
        File addinsDir = new File(PlatformSupport.getToolStorageDirectory(), STORAGE_DIR_ADDINS);
        Utilities.mkdirsOrException(addinsDir);
        return addinsDir;
    }

    public void updateInstalledAddins() {
        availableAddins = new LinkedList<>();

        File addinsDir;
        try {
            addinsDir = getAddinsDir();
        } catch (IOException e) {
            log.log(Level.SEVERE, "No addinsDir", e);
            throw new RuntimeException(e);
        }

        File[] files = addinsDir.listFiles();

        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(GOOMOD_EXTENSION_WITH_DOT)) {
                try {
                    availableAddins.add(AddinFactory.loadAddin(file));
                } catch (AddinFormatException | IOException e) {
                    log.log(Level.WARNING, "Ignoring invalid addin " + file + " in addins dir", e);
                }
            }
        }
    }

    public Configuration readConfiguration() throws IOException {
        Configuration c = new Configuration();
        readGamePreferences(c);
        readPrivateConfig(c);
        return c;
    }

    private void readGamePreferences(Configuration c) throws IOException {
        GamePreferences.readGamePreferences(c, getGameFile(USER_CONFIG_FILE));
    }

    public void writeGamePreferences(Configuration c) throws IOException {
        GamePreferences.writeGamePreferences(c, getCustomGameFile(USER_CONFIG_FILE));
    }

    public abstract void launch() throws IOException;

    public abstract File getWogDir() throws IOException;

    public abstract void setCustomDir(File customDir) throws IOException;

    public abstract File getCustomDir() throws IOException;

    public abstract boolean isFirstCustomBuild() throws IOException;

    private void readPrivateConfig(Configuration c) {
        SharedPreferences p = getPreferences();

//    String versionStr = p.get(WorldOfGoo.PREF_LASTVERSION, null);
//    if (versionStr != null) {
//      VersionSpec lastVersion = new VersionSpec(versionStr);
        // Here we can put any upgrade stuff
//    }

        c.setAllowWidescreen(p.getBoolean(PREF_ALLOW_WIDESCREEN, c.isAllowWidescreen()));
        c.setSkipOpeningMovie(p.getBoolean(PREF_SKIP_OPENING_MOVIE, c.isSkipOpeningMovie()));
        c.setWatermark(p.getString(PREF_WATERMARK, ""));

        String languageStr = p.getString(PREF_LANGUAGE, null);
        if (languageStr != null) c.setLanguage(Language.getLanguageByCode(languageStr));

        Resolution configResolution = c.getResolution();
        int width;
        int height;
        if (configResolution != null) {
            width = p.getInt(PREF_SCREENWIDTH, configResolution.getWidth());
            height = p.getInt(PREF_SCREENHEIGHT, configResolution.getHeight());
            c.setResolution(Resolution.getResolutionByDimensions(width, height));
        } else {
            c.setResolution(Resolution.DEFAULT_RESOLUTION);
        }
        c.setRefreshRate(p.getInt(PREF_REFRESHRATE, 60));
        c.setUiInset(p.getInt(PREF_UIINSET, c.getUiInset()));

        c.setWindowsVolumeControl(p.getBoolean(PREF_WINDOWS_VOLUME_CONTROL, false));

        String addins = p.getString(PREF_ADDINS, null);
        if (addins != null) {
            StringTokenizer tok = new StringTokenizer(addins, ",");
            while (tok.hasMoreTokens()) {
                c.enableAddin(tok.nextToken());
            }
        }
    }

    // ONLY FOR USE BY TEST CASES !!!!!

    public static void DEBUGaddAvailableAddin(Addin a) {
        availableAddins.add(a);
    }

    // ONLY FOR USE BY TEST CASES !!!!!

    public static void DEBUGremoveAddinById(String id) {
        for (Addin availableAddin : availableAddins) {
            if (availableAddin.getId().equals(id)) {
                availableAddins.remove(availableAddin);
                return;
            }
        }
    }

    public static List<Addin> getAvailableAddins() {
        return Collections.unmodifiableList(availableAddins);
    }

    private File getAddinInstalledFile(String addinId) throws IOException {
        return new File(getAddinsDir(), addinId + GOOMOD_EXTENSION_WITH_DOT);
    }


    public void installAddin(File addinFile, String addinId, boolean skipUpdate) throws IOException {
        // If we're skipping the auto-update, we're in a batch process, so don't check the addin already exists
        if (!skipUpdate) {
            // Check we don't already have an addin with this ID
            for (Addin availableAddin : availableAddins) {
                if (availableAddin.getId().equals(addinId)) {
                    throw new DuplicateAddinException("An addin with id " + addinId + " already exists!");
                }
            }
        }

        File destFile = getAddinInstalledFile(addinId);

        log.log(Level.INFO, "Installing addin " + addinId + " from " + addinFile + " to " + destFile);

        Utilities.moveFile(addinFile, destFile);

        if (!skipUpdate)
            updateInstalledAddins();
    }

    public void uninstallAddin(Addin addin, boolean skipUpdate) throws IOException {
        File addinFile = addin.getDiskFile();
        log.log(Level.INFO, "Uninstalling addin, deleting " + addinFile);

        if (!addinFile.delete()) {
            throw new IOException("Couldn't delete " + addinFile);
        }

        if (!skipUpdate)
            updateInstalledAddins();
    }
    //We aren't going to use it on android yet
    //public abstract File chooseCustomDir(Component mainFrame);


    @SuppressWarnings({"UseOfSystemOutOrSystemErr"})
    public static void main(String[] args) throws IOException {
//    init();
//    init(new File("c:\\games\\world of goo"));
        WorldOfGoo worldOfGoo = getTheInstance();
        Configuration c = worldOfGoo.readConfiguration();
        System.out.println("c = " + c);

//    writeConfiguration(c);
    }
}
