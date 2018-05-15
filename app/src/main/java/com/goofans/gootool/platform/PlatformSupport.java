/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.platform;

import com.goofans.gootool.util.Utilities;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import org.meowcat.gootool.WoGInitData;

/**
 * On android there is only one supported platform so a lot of code has been removed.
 *
 * Platform support abstraction class.
 * Also handles setting up an alternative preferences store if -preferences <file> is set on command line.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id: PlatformSupport.java 415 2010-09-09 19:05:48Z david $
 */
public class PlatformSupport
{
  private static final Logger log = Logger.getLogger(PlatformSupport.class.getName());

  public static String[] getProfileSearchPaths()
  {
    return doGetProfileSearchPaths();
  }

  public static String[] doGetProfileSearchPaths() {
    return new String[0];
  }

  public static File getToolStorageDirectory() throws IOException
  {
    File dir = doGetToolStorageDirectory();
    Utilities.mkdirsOrException(dir);
    return dir;
  }
  public static File doGetToolStorageDirectory() throws IOException {
    return WoGInitData.getContext().getExternalFilesDir(null);
  }
}
