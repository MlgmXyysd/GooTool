/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.addins;

import com.goofans.gootool.util.VersionSpec;

import java.util.List;

import mobi.meow.android.gootool.Logger;

/**
 * Immutable after construction.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id: AddinDependency.java 389 2010-05-02 18:03:02Z david $
 */
public class AddinDependency {
    private static final Logger log = Logger.getLogger(AddinDependency.class.getName());

    private final String ref;
    private final VersionSpec minVersion;
    private final VersionSpec maxVersion;

    public AddinDependency(String ref, VersionSpec minVersion, VersionSpec maxVersion) {
        this.ref = ref;
        this.minVersion = minVersion;
        this.maxVersion = maxVersion;
    }

    public String getRef() {
        return ref;
    }

    public VersionSpec getMinVersion() {
        return minVersion;
    }

    public VersionSpec getMaxVersion() {
        return maxVersion;
    }

    public boolean isSatisfiedBy(List<Addin> addins) {
        for (Addin addin : addins) {
            if (addin.getId().equals(ref)) {

                VersionSpec addinVersion = addin.getVersion();

                if (minVersion != null && (addinVersion.compareTo(minVersion) < 0)) {
                    log.fine("Addin " + ref + " version " + addinVersion + " is lower than our min-version " + minVersion);
                    return false;
                }

                if (maxVersion != null && (addinVersion.compareTo(maxVersion) > 0)) {
                    log.fine("Addin " + ref + " version " + addinVersion + " is higher than our max-version " + maxVersion);
                    return false;
                }
                return true;
            }
        }
        log.fine("No addin by ref " + ref + " found");
        return false;
    }


    @Override
    @SuppressWarnings({"StringConcatenation"})
    public String toString() {
        return "AddinDependency{" +
                "ref='" + ref + '\'' +
                ", minVersion=" + minVersion +
                ", maxVersion=" + maxVersion +
                '}';
    }
}
