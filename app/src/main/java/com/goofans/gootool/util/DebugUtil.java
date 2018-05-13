/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.util;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Utilities for use only in test cases (psvm etc).
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id: DebugUtil.java 396 2010-06-11 20:33:38Z david $
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public class DebugUtil
{
  private DebugUtil()
  {
  }

  /**
   * Sets the debugging level of all project classes to the highest.
   */
  public static void setAllLogging()
  {
//    Logger.getLogger("").setLevel(Level.ALL);
    Logger.getLogger("com.goofans").setLevel(Level.ALL);
    Logger.getLogger("net.infotrek").setLevel(Level.ALL);
    Logger.getLogger("").getHandlers()[0].setLevel(Level.ALL);
  }
}
